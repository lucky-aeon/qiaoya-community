package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterTranscriptAssembler;
import org.xhy.community.application.course.dto.AdminChapterTranscriptDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.ChapterTranscriptDomainService;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptProvider;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;
import org.xhy.community.infrastructure.transcript.TranscriptProperties;

import java.util.List;

@Service
public class AdminChapterTranscriptAppService {

    private final ChapterDomainService chapterDomainService;
    private final ChapterTranscriptDomainService transcriptDomainService;
    private final ChapterTranscriptTriggerService triggerService;
    private final ChapterVideoResourceResolver videoResourceResolver;
    private final TranscriptProperties transcriptProperties;

    public AdminChapterTranscriptAppService(ChapterDomainService chapterDomainService,
                                            ChapterTranscriptDomainService transcriptDomainService,
                                            ChapterTranscriptTriggerService triggerService,
                                            ChapterVideoResourceResolver videoResourceResolver,
                                            TranscriptProperties transcriptProperties) {
        this.chapterDomainService = chapterDomainService;
        this.transcriptDomainService = transcriptDomainService;
        this.triggerService = triggerService;
        this.videoResourceResolver = videoResourceResolver;
        this.transcriptProperties = transcriptProperties;
    }

    public AdminChapterTranscriptDTO getTranscript(String chapterId) {
        ChapterTranscriptEntity transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        if (transcript != null && transcript.getProvider() == ChapterTranscriptProvider.NOOP) {
            return ChapterTranscriptAssembler.emptyAdmin(
                    chapterId,
                    "TRANSCRIPT_PROVIDER_NOOP",
                    "历史本地模拟文字稿已隐藏，请重新生成真实转写任务"
            );
        }
        return transcript == null
                ? ChapterTranscriptAssembler.emptyAdmin(chapterId)
                : ChapterTranscriptAssembler.toAdminDTO(transcript);
    }

    public AdminChapterTranscriptDTO retry(String chapterId) {
        ChapterTranscriptEntity transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        if (transcript == null) {
            return regenerate(chapterId);
        }
        if (transcript.getStatus() == ChapterTranscriptStatus.FAILED) {
            transcriptDomainService.resetForRetry(transcript.getId());
        }
        return getTranscript(chapterId);
    }

    public AdminChapterTranscriptDTO regenerate(String chapterId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        if (!transcriptProperties.isEnabled()) {
            return ChapterTranscriptAssembler.emptyAdmin(
                    chapterId,
                    "TRANSCRIPT_DISABLED",
                    "视频转写功能未启用，请设置 TRANSCRIPT_ENABLED=true 后重启后端"
            );
        }
        if (videoResourceResolver.resolveFirstVideoResourceId(chapter.getContent()).isEmpty()) {
            return ChapterTranscriptAssembler.emptyAdmin(
                    chapterId,
                    "NO_VIDEO_RESOURCE",
                    "未检测到章节视频资源，请确认章节内容中包含视频"
            );
        }
        ChapterTranscriptEntity transcript = triggerService.regenerate(chapter);
        return transcript == null
                ? ChapterTranscriptAssembler.emptyAdmin(chapterId)
                : ChapterTranscriptAssembler.toAdminDTO(transcript);
    }

    public int batchGenerateForCourse(String courseId) {
        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);
        int count = 0;
        for (ChapterEntity chapter : chapters) {
            ChapterTranscriptEntity transcript = triggerService.regenerate(chapter);
            if (transcript != null) {
                count++;
            }
        }
        return count;
    }
}
