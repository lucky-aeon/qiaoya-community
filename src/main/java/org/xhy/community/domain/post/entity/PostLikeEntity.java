package org.xhy.community.domain.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;

@TableName("post_likes")
public class PostLikeEntity extends BaseEntity {
    
    private String postId;
    private String userId;
    
    public PostLikeEntity() {
    }
    
    public PostLikeEntity(String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}