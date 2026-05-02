package org.xhy.community.application.course.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterTranscriptAssembler;
import org.xhy.community.application.course.dto.ChapterTranscriptDTO;
import org.xhy.community.application.permission.service.UserPermissionAppService;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptSegmentEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.ChapterTranscriptDomainService;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptProvider;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;

import java.util.List;

@Service
public class ChapterTranscriptAppService {

    private final ChapterDomainService chapterDomainService;
    private final UserPermissionAppService userPermissionAppService;
    private final ChapterTranscriptDomainService transcriptDomainService;
    private final ObjectMapper objectMapper;

    public ChapterTranscriptAppService(ChapterDomainService chapterDomainService,
                                       UserPermissionAppService userPermissionAppService,
                                       ChapterTranscriptDomainService transcriptDomainService,
                                       ObjectMapper objectMapper) {
        this.chapterDomainService = chapterDomainService;
        this.userPermissionAppService = userPermissionAppService;
        this.transcriptDomainService = transcriptDomainService;
        this.objectMapper = objectMapper;
    }

    public ChapterTranscriptDTO getChapterTranscript(String chapterId, String userId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        validateChapterAccess(chapter.getCourseId(), userId);

        ChapterTranscriptEntity transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        if (transcript == null || transcript.getProvider() == ChapterTranscriptProvider.NOOP) {
            return ChapterTranscriptAssembler.empty(chapterId);
        }
        List<ChapterTranscriptSegmentEntity> segments = transcriptDomainService.getSegments(transcript.getId());
        return ChapterTranscriptAssembler.toDTO(transcript, segments, objectMapper);
    }

    private void validateChapterAccess(String courseId, String userId) {
        if (userId == null || !userPermissionAppService.hasAccessToCourse(userId, courseId)) {
            throw new BusinessException(CourseErrorCode.CHAPTER_ACCESS_DENIED);
        }
    }
}
