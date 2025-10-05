package org.xhy.community.application.course.dto;

import java.time.LocalDateTime;

public class CourseProgressDTO {
    private String courseId;
    private Integer totalChapters;
    private Integer completedChapters;
    private Integer progressPercent;
    private String lastAccessChapterId;
    private LocalDateTime lastAccessTime;
    private Boolean completed;
    private LocalDateTime completedAt;
    // 是否拥有“课程完成”标签
    private Boolean hasCompletionTag;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public Integer getTotalChapters() { return totalChapters; }
    public void setTotalChapters(Integer totalChapters) { this.totalChapters = totalChapters; }
    public Integer getCompletedChapters() { return completedChapters; }
    public void setCompletedChapters(Integer completedChapters) { this.completedChapters = completedChapters; }
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public String getLastAccessChapterId() { return lastAccessChapterId; }
    public void setLastAccessChapterId(String lastAccessChapterId) { this.lastAccessChapterId = lastAccessChapterId; }
    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Boolean getHasCompletionTag() { return hasCompletionTag; }
    public void setHasCompletionTag(Boolean hasCompletionTag) { this.hasCompletionTag = hasCompletionTag; }
}
