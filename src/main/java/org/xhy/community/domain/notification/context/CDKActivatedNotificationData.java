package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CDK激活通知数据
 */
public class CDKActivatedNotificationData extends NotificationData {
    
    private final String cdkCode;           // CDK码
    private final String activationTime;   // 激活时间（格式化后的字符串）
    
    public CDKActivatedNotificationData(String recipientId, String recipientName, String recipientEmail,
                                      String cdkCode, LocalDateTime activationTime) {
        super(recipientId, recipientName, recipientEmail, NotificationType.CDK_ACTIVATED);
        this.cdkCode = cdkCode;
        this.activationTime = activationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getCdkCode() { return cdkCode; }
    public String getActivationTime() { return activationTime; }
}