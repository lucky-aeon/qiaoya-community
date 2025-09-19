package org.xhy.community.domain.follow.event;

import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.time.LocalDateTime;

/**
 * 用户关注事件 - 包含完整的通知所需信息
 * 当用户关注某个对象时触发
 */
public class UserFollowedEvent {
    
    private final String followerId;      // 关注者ID
    private final String targetId;        // 被关注对象ID
    private final FollowTargetType targetType;  // 关注类型
    private final LocalDateTime followTime;     // 关注时间
    
    // 包含完整的业务信息，避免通知领域跨领域查询
    private final String followerName;    // 关注者姓名
    private final String followerEmail;   // 关注者邮箱
    private final String targetName;      // 被关注者姓名
    private final String targetEmail;     // 被关注者邮箱
    
    public UserFollowedEvent(String followerId, String targetId, FollowTargetType targetType,
                           String followerName, String followerEmail,
                           String targetName, String targetEmail) {
        this.followerId = followerId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.followerName = followerName;
        this.followerEmail = followerEmail;
        this.targetName = targetName;
        this.targetEmail = targetEmail;
        this.followTime = LocalDateTime.now();
    }
    
    /**
     * 简化构造函数 - 用于兼容现有代码
     * TODO: 后续优化为从Application层传入完整用户信息
     */
    public UserFollowedEvent(String followerId, String targetId, FollowTargetType targetType) {
        this.followerId = followerId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.followerName = "待获取";  // 占位符，通知监听器需要补充
        this.followerEmail = null;
        this.targetName = "待获取";   // 占位符，通知监听器需要补充  
        this.targetEmail = null;
        this.followTime = LocalDateTime.now();
    }
    
    // 基础信息Getters
    public String getFollowerId() { return followerId; }
    public String getTargetId() { return targetId; }
    public FollowTargetType getTargetType() { return targetType; }
    public LocalDateTime getFollowTime() { return followTime; }
    
    // 完整业务信息Getters
    public String getFollowerName() { return followerName; }
    public String getFollowerEmail() { return followerEmail; }
    public String getTargetName() { return targetName; }
    public String getTargetEmail() { return targetEmail; }
}