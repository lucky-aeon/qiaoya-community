package org.xhy.community.domain.subscription.event;

import java.time.LocalDateTime;

public class CourseCDKActivatedEvent {
    
    private final String userId;
    private final String courseId;
    private final String cdkCode;
    private final LocalDateTime activatedTime;
    
    public CourseCDKActivatedEvent(String userId, String courseId, String cdkCode) {
        this.userId = userId;
        this.courseId = courseId;
        this.cdkCode = cdkCode;
        this.activatedTime = LocalDateTime.now();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public String getCdkCode() {
        return cdkCode;
    }
    
    public LocalDateTime getActivatedTime() {
        return activatedTime;
    }
}