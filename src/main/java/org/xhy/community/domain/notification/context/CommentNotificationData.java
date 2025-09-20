package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 评论通知数据
 */
public class CommentNotificationData extends NotificationData {
    
    private final String commenterName;    // 评论者姓名
    private final String targetTitle;      // 被评论内容标题
    private final String targetType;       // 被评论内容类型
    private final String commentContent;   // 评论内容
    private final String targetId;         // 被评论内容ID
    
    public CommentNotificationData(String recipientId, String recipientName, String recipientEmail,
                                 Boolean emailNotificationEnabled, String commenterName, String targetTitle, String targetType,
                                 String commentContent, String targetId) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled,
              targetType.equals("post") ? NotificationType.POST_COMMENT : NotificationType.COURSE_COMMENT);
        this.commenterName = commenterName;
        this.targetTitle = targetTitle;
        this.targetType = targetType;
        this.commentContent = commentContent;
        this.targetId = targetId;
    }
    
    public String getCommenterName() { return commenterName; }
    public String getTargetTitle() { return targetTitle; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getTargetUrl() { 
        return "https://qiaoya.com/" + targetType + "/" + targetId; 
    }
    public String getTruncatedCommentContent() {
        return commentContent.length() > 100 ? commentContent.substring(0, 100) + "..." : commentContent;
    }
}