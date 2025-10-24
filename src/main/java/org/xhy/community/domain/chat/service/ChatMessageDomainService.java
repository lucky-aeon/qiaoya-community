package org.xhy.community.domain.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.xhy.community.domain.chat.entity.ChatMessageEntity;
import org.xhy.community.domain.chat.entity.ChatRoomMemberEntity;
import org.xhy.community.domain.chat.query.ChatMessageQuery;
import org.xhy.community.domain.chat.repository.ChatMessageRepository;
import org.xhy.community.domain.chat.repository.ChatRoomMemberRepository;
import org.xhy.community.domain.chat.event.ChatMessageCreatedEvent;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ChatErrorCode;

@Service
public class ChatMessageDomainService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ChatMessageDomainService(ChatMessageRepository chatMessageRepository,
                                    ChatRoomMemberRepository chatRoomMemberRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发送消息：校验成员、引用归属；保存消息。
     * 事件发布在应用层或后续集成时补充。
     */
    public ChatMessageEntity sendMessage(ChatMessageEntity message) {
        if (message == null) {
            throw new IllegalArgumentException("message 不能为空");
        }
        // 校验发送者是该房间成员
        boolean member = chatRoomMemberRepository.exists(new LambdaQueryWrapper<ChatRoomMemberEntity>()
                .eq(ChatRoomMemberEntity::getRoomId, message.getRoomId())
                .eq(ChatRoomMemberEntity::getUserId, message.getSenderId()));
        if (!member) {
            throw new BusinessException(ChatErrorCode.SENDER_NOT_MEMBER);
        }

        // 校验引用消息归属同一房间
        if (message.getQuotedMessageId() != null) {
            ChatMessageEntity quoted = chatMessageRepository.selectOne(new LambdaQueryWrapper<ChatMessageEntity>()
                    .eq(ChatMessageEntity::getId, message.getQuotedMessageId()));
            if (quoted == null) {
                throw new BusinessException(ChatErrorCode.MESSAGE_NOT_FOUND);
            }
            if (!message.getRoomId().equals(quoted.getRoomId())) {
                throw new BusinessException(ChatErrorCode.QUOTE_CROSS_ROOM_NOT_ALLOWED);
            }
        }

        chatMessageRepository.insert(message);
        // 发布消息创建事件（由 @TransactionalEventListener 在事务提交后处理）
        ChatMessageCreatedEvent event = new ChatMessageCreatedEvent(
                message.getId(), message.getRoomId(), message.getSenderId(), message.getContent(), message.getQuotedMessageId(), message.getMentionedUserIds());
        eventPublisher.publishEvent(event);
        return message;
    }

    /**
     * 分页拉取消息：USER 仅能查询自己加入的房间；ADMIN 无限制。
     */
    public IPage<ChatMessageEntity> pageMessages(ChatMessageQuery query, String operatorId) {
        // 无管理员特权：统一要求查询者为房间成员
        boolean isMember = chatRoomMemberRepository.exists(new LambdaQueryWrapper<ChatRoomMemberEntity>()
                .eq(ChatRoomMemberEntity::getRoomId, query.getRoomId())
                .eq(ChatRoomMemberEntity::getUserId, operatorId));
        if (!isMember) {
            throw new BusinessException(ChatErrorCode.UNAUTHORIZED_ROOM_ACCESS);
        }

        Page<ChatMessageEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getRoomId, query.getRoomId())
                .orderByAsc(ChatMessageEntity::getCreateTime);
        return chatMessageRepository.selectPage(page, wrapper);
    }

    /** 返回第一条在 since 之后的消息（用于未读锚点）。 */
    public ChatMessageEntity findFirstSince(String roomId, java.time.LocalDateTime sinceExclusive) {
        LambdaQueryWrapper<ChatMessageEntity> qw = new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getRoomId, roomId)
                .gt(sinceExclusive != null, ChatMessageEntity::getCreateTime, sinceExclusive)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .orderByAsc(ChatMessageEntity::getId);
        Page<ChatMessageEntity> p = new Page<>(1, 1);
        IPage<ChatMessageEntity> ret = chatMessageRepository.selectPage(p, qw);
        if (ret.getRecords() == null || ret.getRecords().isEmpty()) return null;
        return ret.getRecords().get(0);
    }

    public ChatMessageEntity getById(String messageId) {
        return chatMessageRepository.selectOne(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getId, messageId));
    }

    /** 统计某房间在 since 之后的消息数量（未读数计算）。不排除发送者自身。 */
    public long countSince(String roomId, java.time.LocalDateTime since) {
        if (since == null) {
            return chatMessageRepository.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessageEntity>()
                    .eq(ChatMessageEntity::getRoomId, roomId));
        }
        return chatMessageRepository.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getRoomId, roomId)
                .gt(ChatMessageEntity::getCreateTime, since));
    }

    /** 统计某房间在 since 之后的未读消息数量（排除用户自己发送的消息）。 */
    public long countUnreadForUser(String roomId, java.time.LocalDateTime since, String userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessageEntity> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessageEntity>()
                        .eq(ChatMessageEntity::getRoomId, roomId)
                        .ne(ChatMessageEntity::getSenderId, userId);
        if (since != null) {
            qw.gt(ChatMessageEntity::getCreateTime, since);
        }
        return chatMessageRepository.selectCount(qw);
    }

    /**
     * 批量统计多个房间的未读数（排除用户自己发送的消息）。
     * 策略：若全部 lastSeen 为空，则仅选出 room_id、sender_id 后在内存分组计数；否则以最小 lastSeen 为下限拉取消息并过滤。
     */
    public java.util.Map<String, Long> countUnreadByRoomsForUser(java.util.Set<String> roomIds,
                                                                 java.util.Map<String, java.time.LocalDateTime> lastSeens,
                                                                 String userId) {
        java.util.Map<String, Long> result = new java.util.HashMap<>();
        if (roomIds == null || roomIds.isEmpty()) return result;

        boolean allNull = true;
        java.time.LocalDateTime minSince = null;
        for (String rid : roomIds) {
            java.time.LocalDateTime ls = lastSeens != null ? lastSeens.get(rid) : null;
            if (ls != null) {
                allNull = false;
                if (minSince == null || ls.isBefore(minSince)) minSince = ls;
            }
        }

        if (allNull) {
            java.util.List<ChatMessageEntity> msgs = chatMessageRepository.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessageEntity>()
                            .in(ChatMessageEntity::getRoomId, roomIds)
                            .ne(ChatMessageEntity::getSenderId, userId)
                            .select(ChatMessageEntity::getRoomId, ChatMessageEntity::getSenderId)
            );
            for (String rid : roomIds) result.put(rid, 0L);
            for (ChatMessageEntity m : msgs) {
                String rid = m.getRoomId();
                result.put(rid, result.getOrDefault(rid, 0L) + 1);
            }
            return result;
        }

        // 以最小 lastSeen 为阈值拉取消息，减少扫描量
        java.util.List<ChatMessageEntity> msgs = chatMessageRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessageEntity>()
                        .in(ChatMessageEntity::getRoomId, roomIds)
                        .gt(ChatMessageEntity::getCreateTime, minSince)
                        .ne(ChatMessageEntity::getSenderId, userId)
                        .select(ChatMessageEntity::getRoomId, ChatMessageEntity::getCreateTime, ChatMessageEntity::getSenderId)
        );
        for (String rid : roomIds) result.put(rid, 0L);
        for (ChatMessageEntity m : msgs) {
            String rid = m.getRoomId();
            java.time.LocalDateTime ls = lastSeens != null ? lastSeens.get(rid) : null;
            if (ls == null || m.getCreateTime().isAfter(ls)) {
                result.put(rid, result.getOrDefault(rid, 0L) + 1);
            }
        }
        return result;
    }

    /** 第一条未读（排除自己发送）。 */
    public ChatMessageEntity findFirstUnreadForUser(String roomId, java.time.LocalDateTime sinceExclusive, String userId) {
        LambdaQueryWrapper<ChatMessageEntity> qw = new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getRoomId, roomId)
                .ne(ChatMessageEntity::getSenderId, userId)
                .gt(sinceExclusive != null, ChatMessageEntity::getCreateTime, sinceExclusive)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .orderByAsc(ChatMessageEntity::getId);
        Page<ChatMessageEntity> p = new Page<>(1, 1);
        IPage<ChatMessageEntity> ret = chatMessageRepository.selectPage(p, qw);
        if (ret.getRecords() == null || ret.getRecords().isEmpty()) return null;
        return ret.getRecords().get(0);
    }
}
