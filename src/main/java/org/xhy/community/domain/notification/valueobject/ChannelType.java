package org.xhy.community.domain.notification.valueobject;

/**
 * 通知渠道类型枚举
 */
public enum ChannelType {
    
    IN_APP("站内消息"),
    EMAIL("邮件"),
    SMS("短信");
    
    private final String description;
    
    ChannelType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCode() {
        return this.name();
    }
}