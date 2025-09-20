package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 订阅过期通知数据
 */
public class SubscriptionExpiredNotificationData extends NotificationData {
    
    private final String subscriptionId;   // 订阅ID
    private final long daysRemaining;      // 剩余天数
    
    public SubscriptionExpiredNotificationData(String recipientId, String recipientName, String recipientEmail,
                                             Boolean emailNotificationEnabled, String subscriptionId, long daysRemaining) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled, NotificationType.SUBSCRIPTION_EXPIRED);
        this.subscriptionId = subscriptionId;
        this.daysRemaining = daysRemaining;
    }
    
    public String getSubscriptionId() { return subscriptionId; }
    public long getDaysRemaining() { return daysRemaining; }
    public String getRenewalUrl() { 
        return "https://qiaoya.com/subscription/renew"; 
    }
}