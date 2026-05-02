package org.xhy.community.application.course.transcript;

import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;

import java.util.ArrayList;
import java.util.List;

public class TranscriptProviderTaskResult {

    private ChapterTranscriptStatus status;
    private String text;
    private Long durationMs;
    private List<TranscriptSegmentResult> segments = new ArrayList<>();
    private String rawResultJson;
    private String errorCode;
    private String errorMessage;

    public ChapterTranscriptStatus getStatus() { return status; }
    public void setStatus(ChapterTranscriptStatus status) { this.status = status; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public List<TranscriptSegmentResult> getSegments() { return segments; }
    public void setSegments(List<TranscriptSegmentResult> segments) { this.segments = segments; }

    public String getRawResultJson() { return rawResultJson; }
    public void setRawResultJson(String rawResultJson) { this.rawResultJson = rawResultJson; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
