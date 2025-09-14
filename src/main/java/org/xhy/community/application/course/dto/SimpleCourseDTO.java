package org.xhy.community.application.course.dto;

import org.xhy.community.domain.course.valueobject.CourseStatus;

public class SimpleCourseDTO {
    
    private String id;
    private String title;
    private CourseStatus status;
    
    public SimpleCourseDTO() {
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
}