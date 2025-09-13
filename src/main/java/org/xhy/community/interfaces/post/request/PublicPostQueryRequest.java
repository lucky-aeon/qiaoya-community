package org.xhy.community.interfaces.post.request;

import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.interfaces.common.request.PageRequest;

public class PublicPostQueryRequest extends PageRequest {
    
    private CategoryType categoryType;
    
    public PublicPostQueryRequest() {}
    
    public PublicPostQueryRequest(Integer pageNum, Integer pageSize, CategoryType categoryType) {
        super(pageNum, pageSize);
        this.categoryType = categoryType;
    }
    
    public CategoryType getCategoryType() {
        return categoryType;
    }
    
    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
}