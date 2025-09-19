package org.xhy.community.domain.notification.valueobject;

/**
 * 通知状态枚举
 */
public enum NotificationStatus {
    
    PENDING("待发送"),
    SENT("已发送"),
    READ("已读"),
    FAILED("发送失败");
    
    private final String description;
    
    NotificationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCode() {
        return this.name();
    }
}