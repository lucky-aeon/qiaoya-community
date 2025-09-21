package org.xhy.community.application.comment.dto;

import org.xhy.community.domain.comment.valueobject.BusinessType;

import java.time.LocalDateTime;

public class LatestCommentDTO {

    private String id;
    private String content;
    private String commentUserId;
    private String commentUserName;
    private String replyUserId;
    private String replyUserName;
    private String businessId;
    private BusinessType businessType;
    private String businessTypeName;
    private String businessName;
    private LocalDateTime createTime;

    public LatestCommentDTO() {
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCommentUserId() { return commentUserId; }
    public void setCommentUserId(String commentUserId) { this.commentUserId = commentUserId; }

    public String getCommentUserName() { return commentUserName; }
    public void setCommentUserName(String commentUserName) { this.commentUserName = commentUserName; }

    public String getReplyUserId() { return replyUserId; }
    public void setReplyUserId(String replyUserId) { this.replyUserId = replyUserId; }

    public String getReplyUserName() { return replyUserName; }
    public void setReplyUserName(String replyUserName) { this.replyUserName = replyUserName; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }

    public String getBusinessTypeName() { return businessTypeName; }
    public void setBusinessTypeName(String businessTypeName) { this.businessTypeName = businessTypeName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}