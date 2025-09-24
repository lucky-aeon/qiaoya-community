package org.xhy.community.domain.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

@TableName("post_accepted_comments")
public class PostAcceptedCommentEntity extends BaseEntity {

    private String postId;
    private String commentId;

    public PostAcceptedCommentEntity() {}

    public PostAcceptedCommentEntity(String postId, String commentId) {
        this.postId = postId;
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}

