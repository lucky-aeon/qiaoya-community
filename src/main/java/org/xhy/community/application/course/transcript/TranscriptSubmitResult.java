package org.xhy.community.application.course.transcript;

import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;

public class TranscriptSubmitResult {

    private String providerTaskId;
    private ChapterTranscriptStatus status;

    public TranscriptSubmitResult() {
    }

    public TranscriptSubmitResult(String providerTaskId, ChapterTranscriptStatus status) {
        this.providerTaskId = providerTaskId;
        this.status = status;
    }

    public String getProviderTaskId() { return providerTaskId; }
    public void setProviderTaskId(String providerTaskId) { this.providerTaskId = providerTaskId; }

    public ChapterTranscriptStatus getStatus() { return status; }
    public void setStatus(ChapterTranscriptStatus status) { this.status = status; }
}
