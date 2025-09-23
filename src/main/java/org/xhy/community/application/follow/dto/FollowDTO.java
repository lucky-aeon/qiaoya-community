package org.xhy.community.application.follow.dto;

import org.xhy.community.domain.follow.valueobject.FollowStatus;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.time.LocalDateTime;

/**
 * 关注信息DTO
 */
public class FollowDTO {
    
    private String id;
    private String followerId;
    private String targetId;
    private FollowTargetType targetType;
    private FollowStatus status;
    private LocalDateTime followTime;
    private LocalDateTime unfollowTime;
    
    // 内容展示字段（最小化）
    private String targetName;        // 被关注对象名称
    
    public FollowDTO() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFollowerId() { return followerId; }
    public void setFollowerId(String followerId) { this.followerId = followerId; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public FollowTargetType getTargetType() { return targetType; }
    public void setTargetType(FollowTargetType targetType) { this.targetType = targetType; }
    
    public FollowStatus getStatus() { return status; }
    public void setStatus(FollowStatus status) { this.status = status; }
    
    public LocalDateTime getFollowTime() { return followTime; }
    public void setFollowTime(LocalDateTime followTime) { this.followTime = followTime; }
    
    public LocalDateTime getUnfollowTime() { return unfollowTime; }
    public void setUnfollowTime(LocalDateTime unfollowTime) { this.unfollowTime = unfollowTime; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
}
