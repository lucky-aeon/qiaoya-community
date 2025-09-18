package org.xhy.community.domain.follow.event;

import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.time.LocalDateTime;

/**
 * 用户关注事件
 * 当用户关注某个对象时触发
 */
public class UserFollowedEvent {
    
    private final String followerId;      // 关注者ID
    private final String targetId;        // 被关注对象ID
    private final FollowTargetType targetType;  // 关注类型
    private final LocalDateTime followTime;     // 关注时间
    
    public UserFollowedEvent(String followerId, String targetId, FollowTargetType targetType) {
        this.followerId = followerId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.followTime = LocalDateTime.now();
    }
    
    public String getFollowerId() { return followerId; }
    public String getTargetId() { return targetId; }
    public FollowTargetType getTargetType() { return targetType; }
    public LocalDateTime getFollowTime() { return followTime; }
}