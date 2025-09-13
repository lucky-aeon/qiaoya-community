package org.xhy.community.interfaces.comment.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class CommentQueryRequest extends PageRequest {
    
    public CommentQueryRequest() {
        super();
    }
    
    public CommentQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
}