package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.time.LocalDateTime;

/**
 * 通知数据基类 - 包含完整信息，不依赖其他领域
 */
public abstract class NotificationData {
    
    protected final String recipientId;     // 接收者ID
    protected final String recipientName;   // 接收者姓名
    protected final String recipientEmail;  // 接收者邮箱
    protected final NotificationType type;  // 通知类型
    protected final LocalDateTime createTime; // 创建时间
    
    protected NotificationData(String recipientId, String recipientName, String recipientEmail,
                             NotificationType type) {
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.type = type;
        this.createTime = LocalDateTime.now();
    }
    
    // Getters
    public String getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientEmail() { return recipientEmail; }
    public NotificationType getType() { return type; }
    public LocalDateTime getCreateTime() { return createTime; }
}