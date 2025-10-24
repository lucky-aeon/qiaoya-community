package org.xhy.community.domain.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xhy.community.domain.chat.entity.ChatRoomEntity;
import org.xhy.community.domain.chat.entity.ChatRoomMemberEntity;
import org.xhy.community.domain.chat.query.ChatRoomQuery;
import org.xhy.community.domain.chat.repository.ChatRoomMemberRepository;
import org.xhy.community.domain.chat.repository.ChatRoomRepository;
import org.xhy.community.domain.chat.valueobject.ChatRoomRole;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ChatErrorCode;

@Service
public class ChatRoomDomainService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ChatRoomDomainService(ChatRoomRepository chatRoomRepository,
                                 ChatRoomMemberRepository chatRoomMemberRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建房间并自动加入创建者为 OWNER。
     * 订阅套餐规则由上层编排/或后续补充校验（AccessLevel=USER 仅允许默认套餐）。
     */
    public ChatRoomEntity createRoom(ChatRoomEntity room, String operatorId) {

        chatRoomRepository.insert(room);

        ChatRoomMemberEntity owner = new ChatRoomMemberEntity(room.getId(), operatorId, ChatRoomRole.OWNER);
        chatRoomMemberRepository.insert(owner);
        return room;
    }

    public boolean isMember(String roomId, String userId) {
        return chatRoomMemberRepository.exists(new LambdaQueryWrapper<ChatRoomMemberEntity>()
                .eq(ChatRoomMemberEntity::getRoomId, roomId)
                .eq(ChatRoomMemberEntity::getUserId, userId));
    }

    public ChatRoomEntity getById(String roomId) {
        ChatRoomEntity room = chatRoomRepository.selectOne(new LambdaQueryWrapper<ChatRoomEntity>()
                .eq(ChatRoomEntity::getId, roomId));
        if (room == null) {
            throw new BusinessException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        return room;
    }

    /**
     * 用户加入房间（重复加入幂等）
     */
    public void joinRoom(String roomId, String userId) {
        // 房间存在性校验
        getById(roomId);
        boolean exists = isMember(roomId, userId);
        if (!exists) {
            ChatRoomMemberEntity m = new ChatRoomMemberEntity(roomId, userId, ChatRoomRole.MEMBER);
            chatRoomMemberRepository.insert(m);
        }
    }

    /**
     * 退出房间：成员可退出；房主不允许退出（需转让或解散）。
     */
    public void leaveRoom(String roomId, String userId) {
        // 校验成员关系
        ChatRoomMemberEntity member = chatRoomMemberRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRoomMemberEntity>()
                        .eq(ChatRoomMemberEntity::getRoomId, roomId)
                        .eq(ChatRoomMemberEntity::getUserId, userId)
        );
        if (member == null) {
            throw new BusinessException(ChatErrorCode.UNAUTHORIZED_ROOM_ACCESS);
        }
        if (member.getRole() == ChatRoomRole.OWNER) {
            // 简化规则：房主不能直接退出
            throw new BusinessException(ChatErrorCode.OWNER_CANNOT_LEAVE);
        }
        chatRoomMemberRepository.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRoomMemberEntity>()
                .eq(ChatRoomMemberEntity::getRoomId, roomId)
                .eq(ChatRoomMemberEntity::getUserId, userId));
    }

    /**
     * 解散（删除）房间：仅房主可操作。
     * 逻辑：校验房主 → 清理成员关系 → 逻辑删除房间。
     */
    public void disbandRoom(String roomId, String operatorId) {
        // 校验房间存在
        ChatRoomEntity room = getById(roomId);
        // 校验操作者角色
        ChatRoomMemberEntity operator = chatRoomMemberRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRoomMemberEntity>()
                        .eq(ChatRoomMemberEntity::getRoomId, room.getId())
                        .eq(ChatRoomMemberEntity::getUserId, operatorId)
        );
        if (operator == null) {
            throw new BusinessException(ChatErrorCode.UNAUTHORIZED_ROOM_ACCESS);
        }
        if (operator.getRole() != ChatRoomRole.OWNER) {
            throw new BusinessException(ChatErrorCode.ROOM_DELETE_ONLY_OWNER);
        }

        // 先删除成员关系（物理删除）
        chatRoomMemberRepository.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRoomMemberEntity>()
                .eq(ChatRoomMemberEntity::getRoomId, roomId));
        // 再删除房间（逻辑删除）
        chatRoomRepository.deleteById(roomId);
        // 发布房间删除事件（由 @TransactionalEventListener 在事务提交后推送）
        org.xhy.community.domain.chat.event.ChatRoomDeletedEvent evt = new org.xhy.community.domain.chat.event.ChatRoomDeletedEvent(roomId, operatorId);
        eventPublisher.publishEvent(evt);
    }

    /**
     * 分页：所有房间（管理员或公共列表）
     */
    public IPage<ChatRoomEntity> pageAllRooms(ChatRoomQuery query) {
        Page<ChatRoomEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ChatRoomEntity> wrapper = new LambdaQueryWrapper<ChatRoomEntity>().orderByDesc(ChatRoomEntity::getId);
        return chatRoomRepository.selectPage(page, wrapper);
    }

    /**
     * 批量判断房间是否加入，返回当前用户已加入的房间ID集合
     */
    public java.util.Set<String> getJoinedRoomIds(String userId, java.util.Collection<String> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) return java.util.Collections.emptySet();
        java.util.List<ChatRoomMemberEntity> members = chatRoomMemberRepository.selectList(
                new LambdaQueryWrapper<ChatRoomMemberEntity>()
                        .eq(ChatRoomMemberEntity::getUserId, userId)
                        .in(ChatRoomMemberEntity::getRoomId, roomIds)
        );
        return members.stream().map(ChatRoomMemberEntity::getRoomId).collect(java.util.stream.Collectors.toSet());
    }

    /** 列出房间成员（不分页）。 */
    public java.util.List<ChatRoomMemberEntity> listMembers(String roomId) {
        return chatRoomMemberRepository.selectList(
                new LambdaQueryWrapper<ChatRoomMemberEntity>()
                        .eq(ChatRoomMemberEntity::getRoomId, roomId)
                        .orderByAsc(ChatRoomMemberEntity::getCreateTime)
        );
    }

    /** 统计房间成员数量。 */
    public int countMembers(String roomId) {
        Long cnt = chatRoomMemberRepository.selectCount(
                new LambdaQueryWrapper<ChatRoomMemberEntity>()
                        .eq(ChatRoomMemberEntity::getRoomId, roomId)
        );
        return cnt == null ? 0 : cnt.intValue();
    }

    /**
     * 批量获取房间的成员列表（单次查询），用于在内存分组统计。
     */
    public java.util.List<ChatRoomMemberEntity> listMembersByRooms(java.util.Set<String> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return chatRoomMemberRepository.selectList(
                new LambdaQueryWrapper<ChatRoomMemberEntity>()
                        .in(ChatRoomMemberEntity::getRoomId, roomIds)
        );
    }
}
