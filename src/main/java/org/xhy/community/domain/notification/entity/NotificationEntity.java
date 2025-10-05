package org.xhy.community.domain.notification.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.notification.valueobject.ChannelType;
import org.xhy.community.domain.notification.valueobject.NotificationStatus;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.infrastructure.converter.NotificationTypeConverter;
import org.xhy.community.infrastructure.converter.ChannelTypeConverter;
import org.xhy.community.infrastructure.converter.NotificationStatusConverter;

/**
 * 通知实体
 */
@TableName("notifications")
public class NotificationEntity extends BaseEntity {
    
    /** 接收者ID */
    private String recipientId;
    
    /** 通知类型 */
    @TableField(typeHandler = NotificationTypeConverter.class)
    private NotificationType type;
    
    /** 渠道类型 */
    @TableField(typeHandler = ChannelTypeConverter.class)
    private ChannelType channelType;
    
    /** 通知标题 */
    private String title;
    
    /** 通知内容 */
    private String content;
    
    /** 通知状态 */
    @TableField(typeHandler = NotificationStatusConverter.class)
    private NotificationStatus status;
    
    // Getters and Setters
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public ChannelType getChannelType() { return channelType; }
    public void setChannelType(ChannelType channelType) { this.channelType = channelType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    
    // 业务方法
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }
    
    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }
    
    public void markAsRead() {
        this.status = NotificationStatus.READ;
    }
}
