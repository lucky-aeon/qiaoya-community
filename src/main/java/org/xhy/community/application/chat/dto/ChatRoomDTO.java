package org.xhy.community.application.chat.dto;

import java.time.LocalDateTime;

public class ChatRoomDTO {
    private String id;
    private String name;
    private String description;
    private java.util.List<String> subscriptionPlanIds;
    private String creatorId;
    private org.xhy.community.domain.chat.valueobject.ChatRoomAudience audience;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 是否为当前用户的房间（是否已加入）
    private Boolean joined;
    // 房间成员数量
    private Integer memberCount;
    // 当前用户在该房间的未读消息数量
    private Long unreadCount;

    public ChatRoomDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.List<String> getSubscriptionPlanIds() { return subscriptionPlanIds; }
    public void setSubscriptionPlanIds(java.util.List<String> subscriptionPlanIds) { this.subscriptionPlanIds = subscriptionPlanIds; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public org.xhy.community.domain.chat.valueobject.ChatRoomAudience getAudience() { return audience; }
    public void setAudience(org.xhy.community.domain.chat.valueobject.ChatRoomAudience audience) { this.audience = audience; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Boolean getJoined() { return joined; }
    public void setJoined(Boolean joined) { this.joined = joined; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
}
