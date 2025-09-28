package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 内容更新通知数据
 */
public class ContentUpdateNotificationData extends NotificationData {

    private final String authorName;      // 作者姓名
    private final String contentTitle;    // 内容标题
    private final ContentType contentType; // 内容类型（强类型）
    private final String contentId;       // 内容ID

    public ContentUpdateNotificationData(String recipientId, String recipientName, String recipientEmail,
                                         Boolean emailNotificationEnabled, String authorName, String contentTitle,
                                         ContentType contentType, String contentId) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled, NotificationType.FOLLOWED_USER_POST);
        this.authorName = authorName;
        this.contentTitle = contentTitle;
        this.contentType = contentType;
        this.contentId = contentId;
    }

    public String getAuthorName() { return authorName; }
    public String getContentTitle() { return contentTitle; }
    public ContentType getContentType() { return contentType; }
    public String getContentId() { return contentId; }

    /**
     * 兼容旧模板的兜底方法（未被新注册器使用）
     */
    @Deprecated
    public String getContentUrl() {
        if (contentType == null || contentId == null) return "https://qiaoya.com/";
        return switch (contentType) {
            case POST -> "https://qiaoya.com/dashboard/discussions/" + contentId;
            case COURSE -> "https://qiaoya.com/dashboard/courses/" + contentId;
            case CHAPTER -> "https://qiaoya.com/dashboard/chapters/" + contentId;
            case COMMENT -> "https://qiaoya.com/dashboard/discussions/" + contentId;
        };
    }
}
