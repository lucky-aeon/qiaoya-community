package org.xhy.community.interfaces.post.request;

import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.post.valueobject.PostStatus;

public class PostStatusRequest {
    
    @NotNull(message = "文章状态不能为空")
    private PostStatus status;
    
    public PostStatusRequest() {
    }
    
    // Getters and Setters
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
}