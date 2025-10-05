package org.xhy.community.interfaces.post.request;

import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.interfaces.common.request.PageRequest;

public class AppPostQueryRequest extends PageRequest {
    
    private CategoryType categoryType;
    private String categoryId;
    
    public AppPostQueryRequest() {}
    
    public AppPostQueryRequest(Integer pageNum, Integer pageSize, CategoryType categoryType) {
        super(pageNum, pageSize);
        this.categoryType = categoryType;
    }
    
    public CategoryType getCategoryType() {
        return categoryType;
    }
    
    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
