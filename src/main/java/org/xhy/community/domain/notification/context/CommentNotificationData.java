package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.util.List;

/**
 * 评论通知数据
 */
public class CommentNotificationData extends NotificationData {

    private final String commenterName;      // 评论者姓名
    private final String targetTitle;        // 被评论内容标题
    private final FollowTargetType targetType; // 被评论内容类型（强类型）
    private final String commentContent;     // 评论内容
    private final String targetId;           // 被评论内容ID
    private final boolean reply;             // 是否为“回复评论”场景

    public CommentNotificationData(List<Recipient> recipients, NotificationType type, ContentType contentType, String commenterName, String targetTitle, FollowTargetType targetType, String commentContent, String targetId, boolean reply) {
        super(recipients, type, contentType);
        this.commenterName = commenterName;
        this.targetTitle = targetTitle;
        this.targetType = targetType;
        this.commentContent = commentContent;
        this.targetId = targetId;
        this.reply = reply;
    }

    public String getCommenterName() { return commenterName; }
    public String getTargetTitle() { return targetTitle; }
    public FollowTargetType getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public boolean isReply() { return reply; }

    /**
     * 兼容旧模板的兜底方法（未被新注册器使用）
     */
    @Deprecated
    public String getTargetUrl() {
        if (targetType == null || targetId == null) return "https://code.xhyovo.cn/";
        return switch (targetType) {
            case POST -> "https://code.xhyovo.cn/dashboard/discussions/" + targetId;
            case COURSE -> "https://code.xhyovo.cn/dashboard/courses/" + targetId;
            case CHAPTER -> "https://code.xhyovo.cn/dashboard/chapters/" + targetId;
            default -> "https://code.xhyovo.cn/dashboard/" + targetId;
        };
    }

    public String getTruncatedCommentContent() {
        return commentContent != null && commentContent.length() > 100 ? commentContent.substring(0, 100) + "..." : commentContent;
    }
}
