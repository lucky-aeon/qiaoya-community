package org.xhy.community.application.chat.dto;

import org.xhy.community.domain.chat.valueobject.ChatRoomRole;

import java.time.LocalDateTime;

public class ChatRoomMemberDTO {
    private String userId;
    private String name;
    private String avatar;
    private ChatRoomRole role;
    private Boolean online;
    private LocalDateTime joinedAt;
    private java.util.List<String> tags;

    public ChatRoomMemberDTO() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public ChatRoomRole getRole() { return role; }
    public void setRole(ChatRoomRole role) { this.role = role; }
    public Boolean getOnline() { return online; }
    public void setOnline(Boolean online) { this.online = online; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public java.util.List<String> getTags() { return tags; }
    public void setTags(java.util.List<String> tags) { this.tags = tags; }
}
