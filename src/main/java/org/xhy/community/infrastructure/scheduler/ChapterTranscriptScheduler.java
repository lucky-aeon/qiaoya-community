package org.xhy.community.infrastructure.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhy.community.application.course.service.ChapterTranscriptJobService;

@Component
@ConditionalOnProperty(name = "transcript.enabled", havingValue = "true")
public class ChapterTranscriptScheduler {

    private final ChapterTranscriptJobService jobService;

    public ChapterTranscriptScheduler(ChapterTranscriptJobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(fixedDelayString = "${transcript.poll.fixed-delay-ms:60000}")
    public void submitPendingTasks() {
        jobService.submitPendingTasks();
    }

    @Scheduled(fixedDelayString = "${transcript.poll.fixed-delay-ms:60000}", initialDelayString = "15000")
    public void pollRunningTasks() {
        jobService.pollRunningTasks();
    }
}
