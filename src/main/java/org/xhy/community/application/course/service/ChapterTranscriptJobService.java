package org.xhy.community.application.course.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.transcript.TranscriptProvider;
import org.xhy.community.application.course.transcript.TranscriptProviderTaskResult;
import org.xhy.community.application.course.transcript.TranscriptSegmentResult;
import org.xhy.community.application.course.transcript.TranscriptSubmitCommand;
import org.xhy.community.application.course.transcript.TranscriptSubmitResult;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptSegmentEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.ChapterTranscriptDomainService;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;
import org.xhy.community.domain.resource.service.ResourceDomainService;
import org.xhy.community.infrastructure.transcript.TranscriptProperties;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChapterTranscriptJobService {

    private static final Logger log = LoggerFactory.getLogger(ChapterTranscriptJobService.class);

    private final ChapterTranscriptDomainService transcriptDomainService;
    private final ChapterDomainService chapterDomainService;
    private final ResourceDomainService resourceDomainService;
    private final TranscriptProvider transcriptProvider;
    private final TranscriptProperties transcriptProperties;
    private final ChapterTranscriptSummaryService summaryService;
    private final ObjectMapper objectMapper;

    public ChapterTranscriptJobService(ChapterTranscriptDomainService transcriptDomainService,
                                       ChapterDomainService chapterDomainService,
                                       ResourceDomainService resourceDomainService,
                                       TranscriptProvider transcriptProvider,
                                       TranscriptProperties transcriptProperties,
                                       ChapterTranscriptSummaryService summaryService,
                                       ObjectMapper objectMapper) {
        this.transcriptDomainService = transcriptDomainService;
        this.chapterDomainService = chapterDomainService;
        this.resourceDomainService = resourceDomainService;
        this.transcriptProvider = transcriptProvider;
        this.transcriptProperties = transcriptProperties;
        this.summaryService = summaryService;
        this.objectMapper = objectMapper;
    }

    public void submitPendingTasks() {
        List<ChapterTranscriptEntity> pendingTasks = transcriptDomainService.listPendingForSubmit(
                transcriptProperties.getPoll().getSubmitLimit());
        for (ChapterTranscriptEntity task : pendingTasks) {
            try {
                submitOne(task);
            } catch (Exception e) {
                log.warn("【章节转写】提交失败：transcriptId={}, chapterId={}, resourceId={}, error={}",
                        task.getId(), task.getChapterId(), task.getResourceId(), e.getMessage());
                transcriptDomainService.markFailed(task.getId(), "SUBMIT_FAILED", e.getMessage());
            }
        }
    }

    public void pollRunningTasks() {
        List<ChapterTranscriptEntity> tasks = transcriptDomainService.listSubmittedOrRunningForPoll(
                transcriptProperties.getPoll().getQueryLimit());
        for (ChapterTranscriptEntity task : tasks) {
            try {
                pollOne(task);
            } catch (Exception e) {
                log.warn("【章节转写】轮询失败：transcriptId={}, providerTaskId={}, error={}",
                        task.getId(), task.getProviderTaskId(), e.getMessage());
                transcriptDomainService.markFailed(task.getId(), "POLL_FAILED", e.getMessage());
            }
        }
    }

    private void submitOne(ChapterTranscriptEntity task) {
        ChapterEntity chapter = chapterDomainService.getChapterById(task.getChapterId());
        String fileUrl = resourceDomainService.getOriginDownloadUrl(
                task.getResourceId(),
                transcriptProperties.getFileUrlExpirationSeconds());

        TranscriptSubmitCommand command = new TranscriptSubmitCommand();
        command.setCourseId(task.getCourseId());
        command.setChapterId(task.getChapterId());
        command.setResourceId(task.getResourceId());
        command.setTitle(chapter.getTitle());
        command.setFileUrl(fileUrl);
        command.setLanguage(task.getLanguage());
        command.setModel(task.getModel());

        TranscriptSubmitResult result = transcriptProvider.submit(command);
        if (result == null || result.getProviderTaskId() == null || result.getProviderTaskId().isBlank()) {
            throw new IllegalStateException("供应商未返回 task_id");
        }
        transcriptDomainService.markSubmitted(task.getId(), result.getProviderTaskId());
    }

    private void pollOne(ChapterTranscriptEntity task) throws JsonProcessingException {
        TranscriptProviderTaskResult result = transcriptProvider.query(task.getProviderTaskId());
        if (result == null || result.getStatus() == null) {
            return;
        }

        if (result.getStatus() == ChapterTranscriptStatus.RUNNING || result.getStatus() == ChapterTranscriptStatus.SUBMITTED
                || result.getStatus() == ChapterTranscriptStatus.PENDING) {
            transcriptDomainService.markRunning(task.getId());
            return;
        }

        if (result.getStatus() == ChapterTranscriptStatus.FAILED) {
            transcriptDomainService.markFailed(task.getId(), result.getErrorCode(), result.getErrorMessage());
            return;
        }

        if (result.getStatus() == ChapterTranscriptStatus.SUCCEEDED) {
            ChapterEntity chapter = chapterDomainService.getChapterById(task.getChapterId());
            ChapterTranscriptSummaryService.SummaryResult summary = summaryService.summarize(
                    chapter.getTitle(), result.getText(), result.getSegments());

            List<ChapterTranscriptSegmentEntity> segments = new ArrayList<>();
            if (result.getSegments() != null) {
                for (TranscriptSegmentResult segment : result.getSegments()) {
                    if (segment == null || segment.getText() == null || segment.getText().isBlank()) {
                        continue;
                    }
                    segments.add(ChapterTranscriptSegmentEntity.of(
                            task.getId(),
                            task.getCourseId(),
                            task.getChapterId(),
                            segment.getStartMs(),
                            segment.getEndMs(),
                            segment.getSpeaker(),
                            segment.getText(),
                            segment.getSortOrder() == null ? segments.size() : segment.getSortOrder()
                    ));
                }
            }

            transcriptDomainService.markSucceeded(
                    task.getId(),
                    result.getText(),
                    summary.getSummary(),
                    objectMapper.writeValueAsString(summary.getKeyPoints()),
                    result.getRawResultJson(),
                    result.getDurationMs(),
                    segments
            );
        }
    }
}
