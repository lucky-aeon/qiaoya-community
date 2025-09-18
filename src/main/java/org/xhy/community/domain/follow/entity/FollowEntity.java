package org.xhy.community.domain.follow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.follow.valueobject.FollowStatus;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.infrastructure.converter.FollowStatusConverter;
import org.xhy.community.infrastructure.converter.FollowTargetTypeConverter;

import java.time.LocalDateTime;

@TableName("follows")
public class FollowEntity extends BaseEntity {
    
    /** 关注者用户ID */
    private String followerId;
    
    /** 被关注目标的ID (用户ID/课程ID/章节ID/文章ID) */
    private String targetId;
    
    /** 关注目标类型 */
    @TableField(typeHandler = FollowTargetTypeConverter.class)
    private FollowTargetType targetType;
    
    /** 关注状态 */
    @TableField(typeHandler = FollowStatusConverter.class)
    private FollowStatus status;
    
    /** 关注时间 */
    private LocalDateTime followTime;
    
    /** 取消关注时间 */
    private LocalDateTime unfollowTime;
    
    public FollowEntity() {
    }
    
    public FollowEntity(String followerId, String targetId, FollowTargetType targetType) {
        this.followerId = followerId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.status = FollowStatus.ACTIVE;
        this.followTime = LocalDateTime.now();
    }
    
    /**
     * 取消关注
     */
    public void unfollow() {
        this.status = FollowStatus.CANCELLED;
        this.unfollowTime = LocalDateTime.now();
    }
    
    /**
     * 重新关注
     */
    public void refollow() {
        this.status = FollowStatus.ACTIVE;
        this.followTime = LocalDateTime.now();
        this.unfollowTime = null;
    }
    
    /**
     * 检查是否为活跃关注状态
     */
    public boolean isActive() {
        return FollowStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * 检查是否为已取消状态
     */
    public boolean isCancelled() {
        return FollowStatus.CANCELLED.equals(this.status);
    }
    
    // Getters and Setters
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
}