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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段
    private String followerName;      // 关注者姓名
    private String followerAvatar;    // 关注者头像
    private String targetName;        // 被关注对象名称
    private String targetDescription; // 被关注对象描述
    private String authorName;        // 内容作者名称（针对课程、文章等）
    
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
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    public String getFollowerName() { return followerName; }
    public void setFollowerName(String followerName) { this.followerName = followerName; }
    
    public String getFollowerAvatar() { return followerAvatar; }
    public void setFollowerAvatar(String followerAvatar) { this.followerAvatar = followerAvatar; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public String getTargetDescription() { return targetDescription; }
    public void setTargetDescription(String targetDescription) { this.targetDescription = targetDescription; }
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}