package org.xhy.community.interfaces.post.request;

import org.xhy.community.interfaces.common.request.PageRequest;
import org.xhy.community.domain.post.valueobject.CategoryType;

public class CategoryQueryRequest extends PageRequest {
    
    private CategoryType type;
    private String parentId;
    
    public CategoryQueryRequest() {
    }
    
    // Getters and Setters
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
}