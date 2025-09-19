package org.xhy.community.domain.subscription.event;

import java.time.LocalDateTime;

/**
 * 订阅即将过期事件 - 包含完整的用户和订阅信息
 * 当订阅即将过期时触发，通知用户及时续费
 */
public class SubscriptionExpiringEvent {
    
    private final String userId;          // 用户ID
    private final String userName;        // 用户姓名
    private final String userEmail;       // 用户邮箱
    private final String subscriptionId;  // 订阅ID
    private final long daysRemaining;     // 剩余天数
    private final LocalDateTime expiryDate; // 过期日期
    private final LocalDateTime checkTime;  // 检查时间
    
    public SubscriptionExpiringEvent(String userId, String userName, String userEmail,
                                   String subscriptionId, long daysRemaining, LocalDateTime expiryDate) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.subscriptionId = subscriptionId;
        this.daysRemaining = daysRemaining;
        this.expiryDate = expiryDate;
        this.checkTime = LocalDateTime.now();
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getSubscriptionId() { return subscriptionId; }
    public long getDaysRemaining() { return daysRemaining; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public LocalDateTime getCheckTime() { return checkTime; }
}