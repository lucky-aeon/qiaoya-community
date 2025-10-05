package org.xhy.community.domain.course.event;

import java.time.LocalDateTime;

/**
 * 课程完成事件：当用户首次完成某门课程时发布
 */
public class CourseCompletedEvent {
    private final String userId;
    private final String courseId;
    private final LocalDateTime completedAt;

    public CourseCompletedEvent(String userId, String courseId, LocalDateTime completedAt) {
        this.userId = userId;
        this.courseId = courseId;
        this.completedAt = completedAt == null ? LocalDateTime.now() : completedAt;
    }

    public String getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}

