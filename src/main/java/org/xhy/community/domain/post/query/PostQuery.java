package org.xhy.community.domain.post.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.post.valueobject.CategoryType;

public class PostQuery extends BasePageQuery {
    
    private String authorId;
    private PostStatus status;
    private String categoryId;
    private String title;
    private AccessLevel accessLevel;
    private CategoryType categoryType;
    
    public PostQuery() {
    }
    
    public PostQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public PostStatus getStatus() {
        return status;
    }
    
    public void setStatus(PostStatus status) {
        this.status = status;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }
    
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    public CategoryType getCategoryType() {
        return categoryType;
    }
    
    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
}