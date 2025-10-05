package org.xhy.community.application.course.dto;

import java.time.LocalDateTime;

public class LearningRecordItemDTO {
    private String courseId;
    private String courseTitle;
    private Integer totalChapters;
    private Integer completedChapters;
    private Integer progressPercent;
    private Boolean completed;
    private LocalDateTime completedAt;
    private String lastAccessChapterId;
    private String lastAccessChapterTitle;
    private Integer lastPositionSec;
    private LocalDateTime lastAccessTime;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public Integer getTotalChapters() { return totalChapters; }
    public void setTotalChapters(Integer totalChapters) { this.totalChapters = totalChapters; }
    public Integer getCompletedChapters() { return completedChapters; }
    public void setCompletedChapters(Integer completedChapters) { this.completedChapters = completedChapters; }
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getLastAccessChapterId() { return lastAccessChapterId; }
    public void setLastAccessChapterId(String lastAccessChapterId) { this.lastAccessChapterId = lastAccessChapterId; }
    public String getLastAccessChapterTitle() { return lastAccessChapterTitle; }
    public void setLastAccessChapterTitle(String lastAccessChapterTitle) { this.lastAccessChapterTitle = lastAccessChapterTitle; }
    public Integer getLastPositionSec() { return lastPositionSec; }
    public void setLastPositionSec(Integer lastPositionSec) { this.lastPositionSec = lastPositionSec; }
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }
}

