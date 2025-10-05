package org.xhy.community.domain.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("user_course_progress")
public class UserCourseProgressEntity extends BaseEntity {

    private String userId;
    private String courseId;

    private Integer totalChapters;
    private Integer completedChapters;
    private Integer progressPercent;

    private String lastAccessChapterId;
    private LocalDateTime lastAccessTime;

    private LocalDateTime completedAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

