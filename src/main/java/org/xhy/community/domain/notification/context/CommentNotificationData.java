package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 评论通知数据
 */
public class CommentNotificationData extends NotificationData {

    private final String commenterName;      // 评论者姓名
    private final String targetTitle;        // 被评论内容标题
    private final FollowTargetType targetType; // 被评论内容类型（强类型）
    private final String commentContent;     // 评论内容
    private final String targetId;           // 被评论内容ID

    public CommentNotificationData(String recipientId, String recipientName, String recipientEmail,
                                   Boolean emailNotificationEnabled, String commenterName, String targetTitle, FollowTargetType targetType,
                                   String commentContent, String targetId) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled,
              targetType == FollowTargetType.POST ? NotificationType.POST_COMMENT :
              (targetType == FollowTargetType.COURSE ? NotificationType.COURSE_COMMENT : NotificationType.CHAPTER_COMMENT));
        this.commenterName = commenterName;
        this.targetTitle = targetTitle;
        this.targetType = targetType;
        this.commentContent = commentContent;
        this.targetId = targetId;
    }

    public String getCommenterName() { return commenterName; }
    public String getTargetTitle() { return targetTitle; }
    public FollowTargetType getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }

    /**
     * 兼容旧模板的兜底方法（未被新注册器使用）
     */
    @Deprecated
    public String getTargetUrl() {
        if (targetType == null || targetId == null) return "https://qiaoya.com/";
        return switch (targetType) {
            case POST -> "https://qiaoya.com/dashboard/discussions/" + targetId;
            case COURSE -> "https://qiaoya.com/dashboard/courses/" + targetId;
            case CHAPTER -> "https://qiaoya.com/dashboard/chapters/" + targetId;
            case USER -> "https://qiaoya.com/dashboard/users/" + targetId;
        };
    }

    public String getTruncatedCommentContent() {
        return commentContent != null && commentContent.length() > 100 ? commentContent.substring(0, 100) + "..." : commentContent;
    }
}
