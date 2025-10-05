package org.xhy.community.domain.course.valueobject;

/**
 * 学习进度上报值对象（来自 API 的组合输入）
 */
public class ChapterProgressReport {
    private String userId;
    private String courseId;
    private String chapterId;
    private Integer progressPercent;
    private Integer positionSec;
    private Integer timeSpentDeltaSec;

    public ChapterProgressReport() {}

    public ChapterProgressReport(String userId, String courseId, String chapterId,
                                 Integer progressPercent, Integer positionSec, Integer timeSpentDeltaSec) {
        this.userId = userId;
        this.courseId = courseId;
        this.chapterId = chapterId;
        this.progressPercent = progressPercent;
        this.positionSec = positionSec;
        this.timeSpentDeltaSec = timeSpentDeltaSec;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public Integer getPositionSec() { return positionSec; }
    public void setPositionSec(Integer positionSec) { this.positionSec = positionSec; }
    public Integer getTimeSpentDeltaSec() { return timeSpentDeltaSec; }
    public void setTimeSpentDeltaSec(Integer timeSpentDeltaSec) { this.timeSpentDeltaSec = timeSpentDeltaSec; }
}

