package org.xhy.community.application.course.transcript;

public class TranscriptSegmentResult {

    private Long startMs;
    private Long endMs;
    private String speaker;
    private String text;
    private Integer sortOrder;

    public TranscriptSegmentResult() {
    }

    public TranscriptSegmentResult(Long startMs, Long endMs, String speaker, String text, Integer sortOrder) {
        this.startMs = startMs;
        this.endMs = endMs;
        this.speaker = speaker;
        this.text = text;
        this.sortOrder = sortOrder;
    }

    public Long getStartMs() { return startMs; }
    public void setStartMs(Long startMs) { this.startMs = startMs; }

    public Long getEndMs() { return endMs; }
    public void setEndMs(Long endMs) { this.endMs = endMs; }

    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
