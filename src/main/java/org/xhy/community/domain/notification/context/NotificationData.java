package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据基类 - 包含完整信息，不依赖其他领域
 */
public abstract class NotificationData {
    
    protected final List<Recipient> recipients;  // 接收者邮箱
    protected final NotificationType type;  // 通知类型
    protected final ContentType contentType;

    public NotificationData(List<Recipient> recipients, NotificationType type, ContentType contentType) {
        this.recipients = recipients;
        this.type = type;
        this.contentType = contentType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    // Getters
    public NotificationType getType() { return type; }

    public static class Recipient{
        protected final String recipientId;
        protected final String recipientEmail;
        protected final Boolean emailNotificationEnabled;

        public Recipient(String recipientId, String recipientEmail, Boolean emailNotificationEnabled) {
            this.recipientId = recipientId;
            this.recipientEmail = recipientEmail;
            this.emailNotificationEnabled = emailNotificationEnabled;
        }

        public String getRecipientEmail() {
            return recipientEmail;
        }

        public Boolean getEmailNotificationEnabled() {
            return emailNotificationEnabled;
        }

        public String getRecipientId() {
            return recipientId;
        }
    }

}