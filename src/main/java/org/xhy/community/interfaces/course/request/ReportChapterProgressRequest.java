package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReportChapterProgressRequest {

    @NotBlank
    private String courseId;

    @NotBlank
    private String chapterId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer progressPercent;

    @Min(0)
    private Integer positionSec; // 可选：视频当前位置（秒）

    @Min(0)
    private Integer timeSpentDeltaSec; // 可选：本次新增有效观看时长（秒）

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

