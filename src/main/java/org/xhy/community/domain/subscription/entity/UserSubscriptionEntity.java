package org.xhy.community.domain.subscription.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.subscription.valueobject.SubscriptionStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@TableName("user_subscriptions")
public class UserSubscriptionEntity extends BaseEntity {
    
    private String userId;
    private String subscriptionPlanId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SubscriptionStatus status;
    private String cdkCode;
    
    public UserSubscriptionEntity() {
    }
    
    public UserSubscriptionEntity(String userId, String subscriptionPlanId, 
                                LocalDateTime startTime, LocalDateTime endTime, String cdkCode) {
        this.userId = userId;
        this.subscriptionPlanId = subscriptionPlanId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cdkCode = cdkCode;
        this.status = SubscriptionStatus.ACTIVE;
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return (startTime == null || !now.isBefore(startTime)) && endTime != null && now.isBefore(endTime);
    }
    
    public long getDaysRemaining() {
        LocalDateTime now = LocalDateTime.now();
        if (endTime == null || !now.isBefore(endTime)) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(now, this.endTime));
    }
    
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }
    
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }
    
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return endTime != null && !now.isBefore(endTime);
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSubscriptionPlanId() { return subscriptionPlanId; }
    public void setSubscriptionPlanId(String subscriptionPlanId) { this.subscriptionPlanId = subscriptionPlanId; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    
    public String getCdkCode() { return cdkCode; }
    public void setCdkCode(String cdkCode) { this.cdkCode = cdkCode; }
}
