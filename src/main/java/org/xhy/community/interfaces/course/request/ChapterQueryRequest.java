package org.xhy.community.interfaces.course.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class ChapterQueryRequest extends PageRequest {
    
    private String courseId;
    
    public ChapterQueryRequest() {
    }
    
    // Getters and Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
}