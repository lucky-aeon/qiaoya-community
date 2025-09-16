package org.xhy.community.domain.post.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.post.valueobject.CategoryType;

public class CategoryQuery extends BasePageQuery {
    
    private CategoryType type;
    private String parentId;
    private String name;
    private Boolean isActive;
    
    public CategoryQuery() {
    }
    
    public CategoryQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public CategoryType getType() {
        return type;
    }
    
    public void setType(CategoryType type) {
        this.type = type;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}