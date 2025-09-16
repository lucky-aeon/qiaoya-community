package org.xhy.community.domain.comment.query;

import org.xhy.community.domain.common.query.BasePageQuery;

public class CommentQuery extends BasePageQuery {
    
    private String userId;
    private String postId;
    private String businessId;
    private String businessType;
    
    public CommentQuery() {
    }
    
    public CommentQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPostId() {
        return postId;
    }
    
    public void setPostId(String postId) {
        this.postId = postId;
    }
    
    public String getBusinessId() {
        return businessId;
    }
    
    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
}