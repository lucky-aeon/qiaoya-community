package org.xhy.community.infrastructure.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhy.community.application.transcript.service.ChapterTranscriptAppService;

@Component
public class ChapterTranscriptScheduler {

    private final ChapterTranscriptAppService chapterTranscriptAppService;

    public ChapterTranscriptScheduler(ChapterTranscriptAppService chapterTranscriptAppService) {
        this.chapterTranscriptAppService = chapterTranscriptAppService;
    }

    @Scheduled(fixedDelayString = "${transcript.poll-fixed-delay-ms:30000}")
    public void processTasks() {
        chapterTranscriptAppService.processPendingAndRunning();
    }
}
