package org.xhy.community.interfaces.post.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.xhy.community.domain.post.valueobject.CategoryType;

public class PublicPostQueryRequest {
    
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;
    
    private CategoryType categoryType;
    
    public PublicPostQueryRequest() {}
    
    public PublicPostQueryRequest(Integer page, Integer size, CategoryType categoryType) {
        this.page = page != null ? page : 1;
        this.size = size != null ? size : 10;
        this.categoryType = categoryType;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page != null ? page : 1;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size != null ? size : 10;
    }
    
    public CategoryType getCategoryType() {
        return categoryType;
    }
    
    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
}