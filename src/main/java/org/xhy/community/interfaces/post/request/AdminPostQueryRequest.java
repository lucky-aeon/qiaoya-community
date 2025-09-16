package org.xhy.community.interfaces.post.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 管理员文章查询请求
 * 管理员查询所有用户的文章列表，支持分页
 */
public class AdminPostQueryRequest extends PageRequest {
    
    public AdminPostQueryRequest() {
    }
    
    public AdminPostQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
}