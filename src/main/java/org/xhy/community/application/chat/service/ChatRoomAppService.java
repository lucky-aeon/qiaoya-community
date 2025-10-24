package org.xhy.community.application.chat.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.chat.dto.ChatRoomDTO;
import org.xhy.community.application.chat.assembler.ChatRoomAssembler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.xhy.community.domain.chat.entity.ChatRoomEntity;
import org.xhy.community.domain.chat.entity.ChatRoomMemberEntity;
import org.xhy.community.domain.chat.service.ChatRoomDomainService;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;
import org.xhy.community.interfaces.chat.request.CreateChatRoomRequest;
import org.xhy.community.interfaces.chat.request.ChatRoomQueryRequest;
import org.xhy.community.domain.chat.query.ChatRoomQuery;
import org.xhy.community.application.chat.dto.ChatRoomMemberDTO;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.ws.ChatLiveSessionRegistry;
import org.xhy.community.application.chat.service.ChatUnreadAppService;
import org.xhy.community.domain.chat.service.ChatRoomReadDomainService;
import org.xhy.community.domain.chat.service.ChatMessageDomainService;
import org.xhy.community.domain.tag.service.TagDomainService;
import org.xhy.community.domain.tag.entity.UserTagAssignmentEntity;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.chat.valueobject.ChatRoomAudience;

@Service
public class ChatRoomAppService {

    private final ChatRoomDomainService chatRoomDomainService;
    private final SystemConfigDomainService systemConfigDomainService;
    private final UserDomainService userDomainService;
    private final ChatLiveSessionRegistry liveSessionRegistry;
    private final TagDomainService tagDomainService;
    private final ChatUnreadAppService chatUnreadAppService;
    private final ChatRoomReadDomainService chatRoomReadDomainService;
    private final ChatMessageDomainService chatMessageDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    

    public ChatRoomAppService(ChatRoomDomainService chatRoomDomainService,
                              SystemConfigDomainService systemConfigDomainService,
                              UserDomainService userDomainService,
                              ChatLiveSessionRegistry liveSessionRegistry,
                              TagDomainService tagDomainService,
                              ChatUnreadAppService chatUnreadAppService,
                              ChatRoomReadDomainService chatRoomReadDomainService,
                              ChatMessageDomainService chatMessageDomainService,
                              SubscriptionDomainService subscriptionDomainService,
                              SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.chatRoomDomainService = chatRoomDomainService;
        this.systemConfigDomainService = systemConfigDomainService;
        this.userDomainService = userDomainService;
        this.liveSessionRegistry = liveSessionRegistry;
        this.tagDomainService = tagDomainService;
        this.chatUnreadAppService = chatUnreadAppService;
        this.chatRoomReadDomainService = chatRoomReadDomainService;
        this.chatMessageDomainService = chatMessageDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }

    /**
     * 用户创建房间（默认使用传入的 subscriptionPlanId，默认套餐逻辑后续在编排层接入）。
     */
    public ChatRoomDTO createRoom(CreateChatRoomRequest request, String creatorId, boolean isAdmin) {
        DefaultSubscriptionConfig cfg = systemConfigDomainService.getDefaultSubscriptionConfig();
        if (cfg == null || !cfg.isValid()) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_NOT_FOUND, "默认套餐未配置");
        }
        java.util.List<String> planIds;
        if (isAdmin && request.getSubscriptionPlanIds() != null && !request.getSubscriptionPlanIds().isEmpty()) {
            planIds = new java.util.ArrayList<>(request.getSubscriptionPlanIds());
        } else {
            planIds = java.util.Collections.singletonList(cfg.getSubscriptionPlanId());
        }

        // 受众判定：管理员可显式指定；普通用户按是否持有付费订阅自动确定
        ChatRoomAudience audience;
        if (isAdmin && request.getAudience() != null) {
            audience = request.getAudience();
        } else {
            audience = decideAudienceForUser(creatorId);
        }

        ChatRoomEntity room = ChatRoomAssembler.fromCreateRequest(request, creatorId, planIds, audience);
        ChatRoomEntity created = chatRoomDomainService.createRoom(room, creatorId);
        return ChatRoomAssembler.toDTO(created);
    }

    public void joinRoom(String roomId, String userId) {
        ChatRoomEntity room = chatRoomDomainService.getById(roomId);
        // 优先按受众判定（FREE_ONLY 也允许付费进入：转化为 hasAny）
        ChatRoomAudience audience = room.getAudience();
        if (audience != null) {
            boolean hasAny = subscriptionDomainService.hasAnyActiveSubscription(userId);
            boolean hasPaid = false;
            boolean hasFree = false;
            if (hasAny) {
                java.util.List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
                if (actives != null) {
                    for (UserSubscriptionEntity s : actives) {
                        SubscriptionPlanEntity plan = subscriptionPlanDomainService.getSubscriptionPlanById(s.getSubscriptionPlanId());
                        if (plan.getPrice() != null && plan.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                            hasPaid = true;
                        } else {
                            hasFree = true;
                        }
                    }
                }
            }
            boolean allowed = switch (audience) {
                case PAID_ONLY -> hasPaid;
                // 免费房间允许付费用户进入：等同于 hasAny
                case FREE_ONLY -> hasAny;
                case ALL_USERS -> hasAny;
            };
            if (!allowed) {
                throw new BusinessException(org.xhy.community.infrastructure.exception.ChatErrorCode.PLAN_PERMISSION_DENIED);
            }
        } else {
            // 回退逻辑：按旧的套餐绑定判定
            java.util.List<String> required = room.getSubscriptionPlanIds();
            if (required != null && !required.isEmpty()) {
                java.util.List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
                java.util.Set<String> have = new java.util.HashSet<>();
                if (actives != null) for (UserSubscriptionEntity s : actives) have.add(s.getSubscriptionPlanId());
                boolean allowed = false;
                for (String need : required) { if (have.contains(need)) { allowed = true; break; } }
                if (!allowed) {
                    throw new BusinessException(org.xhy.community.infrastructure.exception.ChatErrorCode.PLAN_PERMISSION_DENIED);
                }
            }
        }
        chatRoomDomainService.joinRoom(roomId, userId);
    }

    private ChatRoomAudience decideAudienceForUser(String userId) {
        java.util.List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        if (actives == null || actives.isEmpty()) {
            // 理论上有默认免费订阅，这里兜底为 FREE_ONLY，避免误放开
            return ChatRoomAudience.FREE_ONLY;
        }
        for (UserSubscriptionEntity s : actives) {
            SubscriptionPlanEntity plan = subscriptionPlanDomainService.getSubscriptionPlanById(s.getSubscriptionPlanId());
            if (plan.getPrice() != null && plan.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                return ChatRoomAudience.PAID_ONLY;
            }
        }
        return ChatRoomAudience.FREE_ONLY;
    }

    /**
     * 删除房间（仅房主）。
     * 应用层负责事务边界。
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteRoom(String roomId, String operatorId) {
        chatRoomDomainService.disbandRoom(roomId, operatorId);
    }

    public boolean isMember(String roomId, String userId) {
        return chatRoomDomainService.isMember(roomId, userId);
    }

    public void leaveRoom(String roomId, String userId) {
        chatRoomDomainService.leaveRoom(roomId, userId);
    }

    public java.util.List<ChatRoomMemberDTO> listRoomMembers(String roomId, String operatorId) {
        // 仅成员可查看
        if (!chatRoomDomainService.isMember(roomId, operatorId)) {
            throw new org.xhy.community.infrastructure.exception.BusinessException(
                    org.xhy.community.infrastructure.exception.ChatErrorCode.UNAUTHORIZED_ROOM_ACCESS);
        }
        java.util.List<ChatRoomMemberEntity> members = chatRoomDomainService.listMembers(roomId);
        java.util.Set<String> uids = members.stream().map(ChatRoomMemberEntity::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(uids);
        java.util.Set<String> onlineUserIds = liveSessionRegistry.getOnlineUserIds(roomId);

        // 聚合用户标签：逐用户取授予记录 -> 汇总 tagId -> 批量取定义 -> 回填名称
        java.util.Map<String, java.util.List<String>> userTagIdsMap = new java.util.HashMap<>();
        java.util.Set<String> allTagIds = new java.util.HashSet<>();
        for (String uid : uids) {
            java.util.List<UserTagAssignmentEntity> assigns = tagDomainService.listIssuedAssignmentsByUser(uid);
            java.util.List<String> tagIds = assigns == null ? java.util.Collections.emptyList()
                    : assigns.stream().map(UserTagAssignmentEntity::getTagId)
                    .filter(java.util.Objects::nonNull).toList();
            userTagIdsMap.put(uid, tagIds);
            allTagIds.addAll(tagIds);
        }
        java.util.Map<String, TagDefinitionEntity> defMap = allTagIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : tagDomainService.getTagDefinitionMapByIds(allTagIds);

        java.util.List<ChatRoomMemberDTO> dtos = new java.util.ArrayList<>(members.size());
        for (ChatRoomMemberEntity m : members) {
            ChatRoomMemberDTO dto = new ChatRoomMemberDTO();
            dto.setUserId(m.getUserId());
            UserEntity u = userMap.get(m.getUserId());
            if (u != null) {
                dto.setName(u.getName());
                dto.setAvatar(u.getAvatar());
            }
            dto.setRole(m.getRole());
            dto.setOnline(onlineUserIds.contains(m.getUserId()));
            dto.setJoinedAt(m.getCreateTime());
            java.util.List<String> tagIds = userTagIdsMap.getOrDefault(m.getUserId(), java.util.Collections.emptyList());
            java.util.List<String> tagNames = tagIds.stream()
                    .map(defMap::get)
                    .filter(java.util.Objects::nonNull)
                    .map(TagDefinitionEntity::getName)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            dto.setTags(tagNames);
            dtos.add(dto);
        }
        // 在线优先，其次按加入时间升序
        dtos.sort(
                java.util.Comparator
                        .comparing((ChatRoomMemberDTO d) -> java.lang.Boolean.TRUE.equals(d.getOnline()))
                        .reversed()
                        .thenComparing(ChatRoomMemberDTO::getJoinedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
        );
        return dtos;
    }

    public IPage<ChatRoomDTO> pageAllRooms(ChatRoomQueryRequest request, String userId) {
        ChatRoomQuery query = ChatRoomAssembler.fromPageRequest(request);
        IPage<ChatRoomEntity> page = chatRoomDomainService.pageAllRooms(query);
        // 批量标记 joined
        java.util.List<ChatRoomEntity> rooms = page.getRecords();
        java.util.Set<String> roomIds = rooms == null ? java.util.Collections.emptySet() : rooms.stream().map(ChatRoomEntity::getId).collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> joined = chatRoomDomainService.getJoinedRoomIds(userId, roomIds);
        IPage<ChatRoomDTO> dtoPage = page.convert(e -> ChatRoomAssembler.toDTOWithJoined(e, joined.contains(e.getId())));
        if (dtoPage.getRecords() != null && !dtoPage.getRecords().isEmpty()) {
            // 批量成员数：单次查询 members, 在内存分组
            java.util.Set<String> pageRoomIds = dtoPage.getRecords().stream().map(ChatRoomDTO::getId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.List<org.xhy.community.domain.chat.entity.ChatRoomMemberEntity> allMembers =
                    chatRoomDomainService.listMembersByRooms(pageRoomIds);
            java.util.Map<String, Integer> memberCountMap = new java.util.HashMap<>();
            for (org.xhy.community.domain.chat.entity.ChatRoomMemberEntity m : allMembers) {
                memberCountMap.merge(m.getRoomId(), 1, Integer::sum);
            }

            // 批量未读数：读取 lastSeenAt，再进行批量统计
            java.util.Map<String, java.time.LocalDateTime> lastSeens = chatRoomReadDomainService.getLastSeenForRooms(userId, pageRoomIds);
            java.util.Map<String, Long> unreadMap = chatMessageDomainService.countUnreadByRoomsForUser(pageRoomIds, lastSeens, userId);

            for (ChatRoomDTO dto : dtoPage.getRecords()) {
                dto.setMemberCount(memberCountMap.getOrDefault(dto.getId(), 0));
                // 未加入的房间未读=0（如需加入前也统计，可移除此判断）
                if (Boolean.TRUE.equals(dto.getJoined())) {
                    dto.setUnreadCount(unreadMap.getOrDefault(dto.getId(), 0L));
                } else {
                    dto.setUnreadCount(0L);
                }
            }
        }
        return dtoPage;
    }
}
