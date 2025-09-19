package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 内容更新通知数据
 */
public class ContentUpdateNotificationData extends NotificationData {
    
    private final String authorName;      // 作者姓名
    private final String contentTitle;   // 内容标题
    private final String contentType;    // 内容类型
    private final String contentId;      // 内容ID
    
    public ContentUpdateNotificationData(String recipientId, String recipientName, String recipientEmail,
                                       String authorName, String contentTitle, String contentType, String contentId) {
        super(recipientId, recipientName, recipientEmail, NotificationType.FOLLOWED_USER_POST);
        this.authorName = authorName;
        this.contentTitle = contentTitle;
        this.contentType = contentType;
        this.contentId = contentId;
    }
    
    public String getAuthorName() { return authorName; }
    public String getContentTitle() { return contentTitle; }
    public String getContentType() { return contentType; }
    public String getContentId() { return contentId; }
    public String getContentUrl() { 
        return "https://qiaoya.com/" + contentType.toLowerCase() + "/" + contentId; 
    }
}