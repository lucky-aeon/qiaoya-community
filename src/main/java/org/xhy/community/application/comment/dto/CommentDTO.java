package org.xhy.community.application.comment.dto;

import org.xhy.community.domain.comment.valueobject.BusinessType;

import java.time.LocalDateTime;

public class CommentDTO {
    
    private String id;
    private String parentCommentId;
    private String rootCommentId;
    private String content;
    private String commentUserId;
    private String replyUserId;
    private String businessId;
    private BusinessType businessType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public CommentDTO() {
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }
    
    public String getRootCommentId() { return rootCommentId; }
    public void setRootCommentId(String rootCommentId) { this.rootCommentId = rootCommentId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCommentUserId() { return commentUserId; }
    public void setCommentUserId(String commentUserId) { this.commentUserId = commentUserId; }
    
    public String getReplyUserId() { return replyUserId; }
    public void setReplyUserId(String replyUserId) { this.replyUserId = replyUserId; }
    
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    
    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}