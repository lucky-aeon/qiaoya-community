package org.xhy.community.domain.comment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.infrastructure.converter.BusinessTypeConverter;

@TableName("comments")
public class CommentEntity extends BaseEntity {
    
    /** 父评论ID（直接回复的评论） */
    private String parentCommentId;
    
    /** 根评论ID（楼主评论） */
    private String rootCommentId;
    
    /** 评论内容 */
    private String content;
    
    /** 评论用户ID */
    private String commentUserId;
    
    /** 被回复用户ID */
    private String replyUserId;
    
    /** 业务ID（文章ID、课程ID等） */
    private String businessId;
    
    /** 业务类型：POST-文章，COURSE-课程 */
    @TableField(typeHandler = BusinessTypeConverter.class)
    private BusinessType businessType;
    
    public CommentEntity() {
    }
    
    public CommentEntity(String content, String commentUserId, String businessId, BusinessType businessType) {
        this.content = content;
        this.commentUserId = commentUserId;
        this.businessId = businessId;
        this.businessType = businessType;
    }
    
    public CommentEntity(String content, String commentUserId, String businessId, BusinessType businessType, 
                        String parentCommentId, String rootCommentId, String replyUserId) {
        this(content, commentUserId, businessId, businessType);
        this.parentCommentId = parentCommentId;
        this.rootCommentId = rootCommentId;
        this.replyUserId = replyUserId;
    }
    
    public boolean isRootComment() {
        return parentCommentId == null;
    }
    
    public boolean isReplyComment() {
        return parentCommentId != null;
    }
    
    // Getters and Setters
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
}