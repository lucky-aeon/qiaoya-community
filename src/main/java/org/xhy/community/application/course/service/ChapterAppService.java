package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterAssembler;
import org.xhy.community.application.course.dto.FrontChapterDetailDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;

/**
 * 前台章节应用服务
 * 提供面向前台用户的章节查询功能
 */
@Service
public class ChapterAppService {

    private final ChapterDomainService chapterDomainService;
    private final CourseDomainService courseDomainService;

    public ChapterAppService(ChapterDomainService chapterDomainService,
                            CourseDomainService courseDomainService) {
        this.chapterDomainService = chapterDomainService;
        this.courseDomainService = courseDomainService;
    }

    /**
     * 根据章节ID获取章节详情
     *
     * @param chapterId 章节ID
     * @return 章节详情信息，包含课程名称
     */
    public FrontChapterDetailDTO getChapterById(String chapterId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        CourseEntity course = courseDomainService.getCourseById(chapter.getCourseId());

        return ChapterAssembler.toFrontDetailDTO(chapter, course.getTitle());
    }
}