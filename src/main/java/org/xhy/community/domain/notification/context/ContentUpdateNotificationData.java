package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.util.List;

/**
 * 内容更新通知数据
 */
public class ContentUpdateNotificationData extends NotificationData {

    private final String authorName;      // 作者姓名
    private final String contentTitle;    // 内容标题
    private final String contentId;       // 内容ID

    public ContentUpdateNotificationData(List<Recipient> recipients, NotificationType type, ContentType contentType, String authorName, String contentTitle, String contentId) {
        super(recipients, type, contentType);
        this.authorName = authorName;
        this.contentTitle = contentTitle;
        this.contentId = contentId;
    }

    public String getAuthorName() { return authorName; }
    public String getContentTitle() { return contentTitle; }
    public String getContentId() { return contentId; }

    /**
     * 兼容旧模板的兜底方法（未被新注册器使用）
     */
    @Deprecated
    public String getContentUrl() {
        if (contentType == null || contentId == null) return "https://code.xhyovo.cn/";
        return switch (contentType) {
            case POST -> "https://code.xhyovo.cn/dashboard/discussions/" + contentId;
            case COURSE -> "https://code.xhyovo.cn/dashboard/courses/" + contentId;
            case CHAPTER -> "https://code.xhyovo.cn/dashboard/chapters/" + contentId;
            case COMMENT -> "https://code.xhyovo.cn/dashboard/discussions/" + contentId;
            case UPDATE_LOG -> null;
            case INTERVIEW_QUESTION -> "https://code.xhyovo.cn/dashboard/interviews/" + contentId;
            case PUBLISH_CONTENT -> null;
            case CHAT_MESSAGE -> null;
        };
    }
}
