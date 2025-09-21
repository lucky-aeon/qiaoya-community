package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterAssembler;
import org.xhy.community.application.course.dto.FrontChapterDetailDTO;
import org.xhy.community.application.course.dto.LatestChapterDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * 获取最新的5条课程章节
     *
     * @return 最新章节列表，包含课程名称
     */
    public List<LatestChapterDTO> getLatestChapters() {
        List<ChapterEntity> chapters = chapterDomainService.getLatestChapters();

        if (chapters.isEmpty()) {
            return List.of();
        }

        // 批量查询课程信息
        Set<String> courseIds = chapters.stream()
                .map(ChapterEntity::getCourseId)
                .collect(Collectors.toSet());
        Map<String, String> courseTitleMap = courseDomainService.getCourseTitleMapByIds(courseIds);

        return chapters.stream()
                .map(chapter -> convertToLatestChapterDTO(chapter, courseTitleMap))
                .collect(Collectors.toList());
    }

    private LatestChapterDTO convertToLatestChapterDTO(ChapterEntity chapter, Map<String, String> courseTitleMap) {
        LatestChapterDTO dto = new LatestChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setCourseId(chapter.getCourseId());
        dto.setCourseName(courseTitleMap.get(chapter.getCourseId()));
        dto.setSortOrder(chapter.getSortOrder());
        dto.setReadingTime(chapter.getReadingTime());
        dto.setCreateTime(chapter.getCreateTime());

        return dto;
    }
}