package org.xhy.community.interfaces.post.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.xhy.community.domain.post.valueobject.PostStatus;

public class PostQueryRequest {
    
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
    
    private PostStatus status;
    
    public PostQueryRequest() {
    }
    
    // Getters and Setters
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
}