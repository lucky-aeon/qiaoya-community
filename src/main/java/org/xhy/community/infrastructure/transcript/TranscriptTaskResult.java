package org.xhy.community.infrastructure.transcript;

import java.util.ArrayList;
import java.util.List;

public class TranscriptTaskResult {

    private String taskStatus;
    private String rawJson;
    private String transcriptionUrl;
    private String resultJson;
    private String text;
    private Long durationMs;
    private String errorCode;
    private String errorMessage;
    private List<Segment> segments = new ArrayList<>();

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    public String getTranscriptionUrl() { return transcriptionUrl; }
    public void setTranscriptionUrl(String transcriptionUrl) { this.transcriptionUrl = transcriptionUrl; }

    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }

    public static class Segment {
        private Long startMs;
        private Long endMs;
        private String speaker;
        private String text;

        public Long getStartMs() { return startMs; }
        public void setStartMs(Long startMs) { this.startMs = startMs; }

        public Long getEndMs() { return endMs; }
        public void setEndMs(Long endMs) { this.endMs = endMs; }

        public String getSpeaker() { return speaker; }
        public void setSpeaker(String speaker) { this.speaker = speaker; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
