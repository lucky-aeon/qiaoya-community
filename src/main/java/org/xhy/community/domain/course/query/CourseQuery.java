package org.xhy.community.domain.course.query;

import org.xhy.community.domain.common.query.BasePageQuery;

public class CourseQuery extends BasePageQuery {
    
    private String title;
    private String categoryId;

    public CourseQuery() {
    }
    
    public CourseQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
}