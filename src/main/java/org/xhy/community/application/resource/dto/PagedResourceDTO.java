package org.xhy.community.application.resource.dto;

import java.util.List;

public class PagedResourceDTO {
    
    private List<ResourceDTO> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    
    public List<ResourceDTO> getRecords() {
        return records;
    }
    
    public void setRecords(List<ResourceDTO> records) {
        this.records = records;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public void setTotal(Long total) {
        this.total = total;
    }
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getPages() {
        return pages;
    }
    
    public void setPages(Integer pages) {
        this.pages = pages;
    }
}