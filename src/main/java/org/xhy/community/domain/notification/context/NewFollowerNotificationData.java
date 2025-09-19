package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 新关注者通知数据 - 从事件构建，包含所有需要的信息
 */
public class NewFollowerNotificationData extends NotificationData {
    
    private final String followerId;      // 关注者ID
    private final String followerName;   // 关注者姓名
    
    public NewFollowerNotificationData(String recipientId, String recipientName, String recipientEmail,
                                     String followerId, String followerName) {
        super(recipientId, recipientName, recipientEmail, NotificationType.NEW_FOLLOWER);
        this.followerId = followerId;
        this.followerName = followerName;
    }
    
    public String getFollowerId() { return followerId; }
    public String getFollowerName() { return followerName; }
    public String getFollowerProfileUrl() { 
        return "https://qiaoya.com/profile/" + followerId; 
    }
}