package org.xhy.community.application.transcript.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChapterTranscriptDTO {

    private String chapterId;
    private String resourceId;
    private String status;
    private Long durationMs;
    private String text;
    private String summary;
    private List<String> keyPoints;
    private List<ChapterTranscriptSectionDTO> sections;
    private List<ChapterTranscriptSegmentDTO> segments;
    private LocalDateTime completedAt;

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }

    public List<ChapterTranscriptSectionDTO> getSections() { return sections; }
    public void setSections(List<ChapterTranscriptSectionDTO> sections) { this.sections = sections; }

    public List<ChapterTranscriptSegmentDTO> getSegments() { return segments; }
    public void setSegments(List<ChapterTranscriptSegmentDTO> segments) { this.segments = segments; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
