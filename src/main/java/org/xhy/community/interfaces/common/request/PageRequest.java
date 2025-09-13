package org.xhy.community.interfaces.common.request;

public class PageRequest {
    
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    public PageRequest() {
    }
    
    public PageRequest(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum != null ? pageNum : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
    }
    
    // Getters and Setters
    public Integer getPageNum() { 
        return pageNum; 
    }
    
    public void setPageNum(Integer pageNum) { 
        this.pageNum = pageNum != null ? pageNum : 1; 
    }
    
    public Integer getPageSize() { 
        return pageSize; 
    }
    
    public void setPageSize(Integer pageSize) { 
        this.pageSize = pageSize != null ? pageSize : 10; 
    }
}