package org.xhy.community.domain.comment.event;

import java.time.LocalDateTime;

/**
 * 评论创建事件 - 包含完整的评论和被评论内容信息
 * 当创建评论时触发，通知被评论内容的作者
 */
public class CommentCreatedEvent {
    
    private final String commentId;       // 评论ID
    private final String commentContent;  // 评论内容
    private final String commenterId;     // 评论者ID
    private final String commenterName;   // 评论者姓名
    private final String commenterEmail;  // 评论者邮箱
    
    // 被评论的内容信息
    private final String targetId;        // 被评论内容ID
    private final String targetType;      // 被评论内容类型 ("post" or "course")
    private final String targetTitle;     // 被评论内容标题
    private final String targetAuthorId;  // 被评论内容作者ID
    private final String targetAuthorName; // 被评论内容作者姓名
    private final String targetAuthorEmail; // 被评论内容作者邮箱
    
    private final LocalDateTime commentTime; // 评论时间
    
    public CommentCreatedEvent(String commentId, String commentContent, 
                             String commenterId, String commenterName, String commenterEmail,
                             String targetId, String targetType, String targetTitle,
                             String targetAuthorId, String targetAuthorName, String targetAuthorEmail) {
        this.commentId = commentId;
        this.commentContent = commentContent;
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commenterEmail = commenterEmail;
        this.targetId = targetId;
        this.targetType = targetType;
        this.targetTitle = targetTitle;
        this.targetAuthorId = targetAuthorId;
        this.targetAuthorName = targetAuthorName;
        this.targetAuthorEmail = targetAuthorEmail;
        this.commentTime = LocalDateTime.now();
    }
    
    // 评论信息Getters
    public String getCommentId() { return commentId; }
    public String getCommentContent() { return commentContent; }
    public String getCommenterId() { return commenterId; }
    public String getCommenterName() { return commenterName; }
    public String getCommenterEmail() { return commenterEmail; }
    
    // 被评论内容信息Getters
    public String getTargetId() { return targetId; }
    public String getTargetType() { return targetType; }
    public String getTargetTitle() { return targetTitle; }
    public String getTargetAuthorId() { return targetAuthorId; }
    public String getTargetAuthorName() { return targetAuthorName; }
    public String getTargetAuthorEmail() { return targetAuthorEmail; }
    
    public LocalDateTime getCommentTime() { return commentTime; }
}