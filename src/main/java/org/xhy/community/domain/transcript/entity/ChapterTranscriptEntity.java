package org.xhy.community.domain.transcript.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("chapter_transcripts")
public class ChapterTranscriptEntity extends BaseEntity {

    private String courseId;
    private String chapterId;
    private String resourceId;
    private String provider;
    private String model;
    private String providerTaskId;
    private String status;
    private String language;
    private Long durationMs;
    private String text;
    private String summary;
    private String keyPointsJson;
    private String outlineJson;
    private String rawResultJson;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getProviderTaskId() { return providerTaskId; }
    public void setProviderTaskId(String providerTaskId) { this.providerTaskId = providerTaskId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getKeyPointsJson() { return keyPointsJson; }
    public void setKeyPointsJson(String keyPointsJson) { this.keyPointsJson = keyPointsJson; }

    public String getOutlineJson() { return outlineJson; }
    public void setOutlineJson(String outlineJson) { this.outlineJson = outlineJson; }

    public String getRawResultJson() { return rawResultJson; }
    public void setRawResultJson(String rawResultJson) { this.rawResultJson = rawResultJson; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
