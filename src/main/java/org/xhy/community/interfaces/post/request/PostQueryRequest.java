package org.xhy.community.interfaces.post.request;

import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.interfaces.common.request.PageRequest;

public class PostQueryRequest extends PageRequest {
    
    private PostStatus status;
    
    public PostQueryRequest() {
    }
    
    // Getters and Setters
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
}