package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.course.service.ChapterTranscriptDomainService;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptProvider;
import org.xhy.community.infrastructure.transcript.TranscriptProperties;

import java.util.Optional;

@Service
public class ChapterTranscriptTriggerService {

    private final ChapterVideoResourceResolver videoResourceResolver;
    private final ChapterTranscriptDomainService transcriptDomainService;
    private final TranscriptProperties transcriptProperties;

    public ChapterTranscriptTriggerService(ChapterVideoResourceResolver videoResourceResolver,
                                           ChapterTranscriptDomainService transcriptDomainService,
                                           TranscriptProperties transcriptProperties) {
        this.videoResourceResolver = videoResourceResolver;
        this.transcriptDomainService = transcriptDomainService;
        this.transcriptProperties = transcriptProperties;
    }

    @Transactional
    public void handleChapterSaved(ChapterEntity before, ChapterEntity saved) {
        if (saved == null) {
            return;
        }
        if (!transcriptProperties.isEnabled()) {
            return;
        }
        Optional<String> oldResourceId = before == null
                ? Optional.empty()
                : videoResourceResolver.resolveFirstVideoResourceId(before.getContent());
        Optional<String> newResourceId = videoResourceResolver.resolveFirstVideoResourceId(saved.getContent());

        if (newResourceId.isEmpty()) {
            if (oldResourceId.isPresent()) {
                transcriptDomainService.softDeleteActiveForChapter(saved.getId());
            }
            return;
        }

        if (oldResourceId.isPresent() && !oldResourceId.get().equals(newResourceId.get())) {
            transcriptDomainService.softDeleteActiveForChapter(saved.getId());
        }

        transcriptDomainService.createPendingIfAbsent(
                saved.getCourseId(),
                saved.getId(),
                newResourceId.get(),
                mapProvider(transcriptProperties.getProvider()),
                transcriptProperties.getModel(),
                transcriptProperties.getLanguage()
        );
    }

    @Transactional
    public ChapterTranscriptEntity regenerate(ChapterEntity chapter) {
        if (!transcriptProperties.isEnabled()) {
            return null;
        }
        transcriptDomainService.softDeleteActiveForChapter(chapter.getId());
        Optional<String> resourceId = videoResourceResolver.resolveFirstVideoResourceId(chapter.getContent());
        if (resourceId.isEmpty()) {
            return null;
        }
        return transcriptDomainService.createPendingIfAbsent(
                chapter.getCourseId(),
                chapter.getId(),
                resourceId.get(),
                mapProvider(transcriptProperties.getProvider()),
                transcriptProperties.getModel(),
                transcriptProperties.getLanguage()
        );
    }

    private ChapterTranscriptProvider mapProvider(String provider) {
        if ("dashscope-qwen-asr".equalsIgnoreCase(provider) || "bailian".equalsIgnoreCase(provider)) {
            return ChapterTranscriptProvider.DASHSCOPE_QWEN_ASR;
        }
        if ("dashscope-fun-asr".equalsIgnoreCase(provider)) {
            return ChapterTranscriptProvider.DASHSCOPE_FUN_ASR;
        }
        if ("tingwu".equalsIgnoreCase(provider)) {
            return ChapterTranscriptProvider.TINGWU;
        }
        if ("noop".equalsIgnoreCase(provider)) {
            return ChapterTranscriptProvider.NOOP;
        }
        throw new IllegalArgumentException("不支持的转写服务配置: " + provider);
    }
}
