package org.xhy.community.domain.subscription.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订阅即将过期批量事件 - 包含批量用户和订阅信息
 * 当检测到多个订阅即将过期时触发，批量通知用户及时续费
 */
public class SubscriptionExpiringEvent {

    private final List<ExpiringSubscription> subscriptions;  // 批量即将过期的订阅
    private final LocalDateTime checkTime;                   // 检查时间
    private final int totalCount;                           // 总数量

    public SubscriptionExpiringEvent(List<ExpiringSubscription> subscriptions, LocalDateTime checkTime) {
        this.subscriptions = subscriptions;
        this.checkTime = checkTime;
        this.totalCount = subscriptions != null ? subscriptions.size() : 0;
    }

    // Getters
    public List<ExpiringSubscription> getSubscriptions() { return subscriptions; }
    public LocalDateTime getCheckTime() { return checkTime; }
    public int getTotalCount() { return totalCount; }

    /**
     * 即将过期的订阅信息
     */
    public static class ExpiringSubscription {
        private final String userId;          // 用户ID
        private final String userName;        // 用户姓名
        private final String userEmail;       // 用户邮箱
        private final String subscriptionId;  // 订阅ID
        private final long daysRemaining;     // 剩余天数
        private final LocalDateTime expiryDate; // 过期日期

        public ExpiringSubscription(String userId, String userName, String userEmail,
                                  String subscriptionId, long daysRemaining, LocalDateTime expiryDate) {
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.subscriptionId = subscriptionId;
            this.daysRemaining = daysRemaining;
            this.expiryDate = expiryDate;
        }

        // Getters
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public String getSubscriptionId() { return subscriptionId; }
        public long getDaysRemaining() { return daysRemaining; }
        public LocalDateTime getExpiryDate() { return expiryDate; }
    }
}