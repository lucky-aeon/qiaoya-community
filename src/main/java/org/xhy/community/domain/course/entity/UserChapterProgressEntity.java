package org.xhy.community.domain.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("user_chapter_progress")
public class UserChapterProgressEntity extends BaseEntity {

    private String userId;
    private String courseId;
    private String chapterId;

    private Integer progressPercent; // 0-100
    private Integer lastPositionSec; // 视频当前位置
    private Integer timeSpentSec;    // 累计有效学习时长

    private LocalDateTime lastAccessTime;
    private LocalDateTime completedAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public Integer getLastPositionSec() { return lastPositionSec; }
    public void setLastPositionSec(Integer lastPositionSec) { this.lastPositionSec = lastPositionSec; }

    public Integer getTimeSpentSec() { return timeSpentSec; }
    public void setTimeSpentSec(Integer timeSpentSec) { this.timeSpentSec = timeSpentSec; }

    public LocalDateTime getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

