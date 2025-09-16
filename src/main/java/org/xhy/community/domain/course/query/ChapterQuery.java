package org.xhy.community.domain.course.query;

import org.xhy.community.domain.common.query.BasePageQuery;

public class ChapterQuery extends BasePageQuery {
    
    private String courseId;
    private String title;
    
    public ChapterQuery() {
    }
    
    public ChapterQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}