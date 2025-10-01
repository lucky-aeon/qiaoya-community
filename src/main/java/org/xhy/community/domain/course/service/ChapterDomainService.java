package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.repository.ChapterRepository;
import org.xhy.community.domain.course.repository.CourseRepository;
import org.xhy.community.domain.common.event.ContentPublishedEvent;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;
import org.xhy.community.domain.course.query.ChapterQuery;

import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChapterDomainService {
    
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ChapterDomainService(ChapterRepository chapterRepository,
                                CourseRepository courseRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.chapterRepository = chapterRepository;
        this.courseRepository = courseRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public ChapterEntity createChapter(ChapterEntity chapter) {
        CourseEntity course = courseRepository.selectById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        
        chapterRepository.insert(chapter);

        // 发布简化的章节创建事件
        publishContentEvent(chapter, course.getAuthorId());

        return chapter;
    }
    
    public ChapterEntity updateChapter(ChapterEntity chapter) {
        CourseEntity course = courseRepository.selectById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        
        chapterRepository.updateById(chapter);
        return chapter;
    }
    
    public void deleteChapter(String chapterId) {
        chapterRepository.deleteById(chapterId);
    }
    
    public ChapterEntity getChapterById(String chapterId) {
        ChapterEntity chapter = chapterRepository.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(CourseErrorCode.CHAPTER_NOT_FOUND);
        }
        return chapter;
    }
    
    public List<ChapterEntity> getChaptersByCourseId(String courseId) {
        LambdaQueryWrapper<ChapterEntity> queryWrapper = new LambdaQueryWrapper<ChapterEntity>()
            .eq(ChapterEntity::getCourseId, courseId)
            .orderByAsc(ChapterEntity::getSortOrder);
        
        return chapterRepository.selectList(queryWrapper);
    }

    /**
     * 批量根据课程ID集合查询章节列表
     * 用于列表页做章节数量与阅读时长聚合，避免 N+1 查询
     */
    public List<ChapterEntity> getChaptersByCourseIds(java.util.Collection<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return java.util.List.of();
        }
        LambdaQueryWrapper<ChapterEntity> queryWrapper = new LambdaQueryWrapper<ChapterEntity>()
                .in(ChapterEntity::getCourseId, courseIds);
        return chapterRepository.selectList(queryWrapper);
    }
    
    public IPage<ChapterEntity> getPagedChapters(ChapterQuery query) {
        Page<ChapterEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<ChapterEntity> queryWrapper = new LambdaQueryWrapper<ChapterEntity>()
            .eq(StringUtils.hasText(query.getCourseId()), ChapterEntity::getCourseId, query.getCourseId())
            .like(StringUtils.hasText(query.getTitle()), ChapterEntity::getTitle, query.getTitle())
            .orderByAsc(ChapterEntity::getSortOrder)
            .orderByDesc(ChapterEntity::getCreateTime);
        
        return chapterRepository.selectPage(page, queryWrapper);
    }
    
    public void batchUpdateChapterOrder(List<String> chapterIds) {
        int totalCount = chapterIds.size();
        for (int i = 0; i < chapterIds.size(); i++) {
            String chapterId = chapterIds.get(i);
            int sortOrder = totalCount - i;

            ChapterEntity chapter = new ChapterEntity();
            chapter.setId(chapterId);
            chapter.setSortOrder(sortOrder);
            chapterRepository.updateById(chapter);
        }
    }

    public List<ChapterEntity> getLatestChapters() {
        return chapterRepository.selectList(
            new LambdaQueryWrapper<ChapterEntity>()
                .orderByDesc(ChapterEntity::getCreateTime)
                .last("LIMIT 5")
        );
    }

    public Map<String, String> getChapterTitleMapByIds(Collection<String> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return Map.of();
        }

        List<ChapterEntity> chapters = chapterRepository.selectBatchIds(chapterIds);
        return chapters.stream()
                .collect(Collectors.toMap(
                    ChapterEntity::getId,
                    ChapterEntity::getTitle
                ));
    }

    /**
     * 批量查询章节所属课程ID映射
     */
    public Map<String, String> getChapterCourseIdMapByIds(Collection<String> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return Map.of();
        }
        List<ChapterEntity> chapters = chapterRepository.selectBatchIds(chapterIds);
        return chapters.stream()
                .collect(Collectors.toMap(
                        ChapterEntity::getId,
                        ChapterEntity::getCourseId
                ));
    }

    /**
     * 发布简化的章节内容事件
     * 只包含必要的标识信息，由Application层统一处理通知逻辑
     */
    private void publishContentEvent(ChapterEntity chapter, String authorId) {
        try {
            ContentPublishedEvent event = new ContentPublishedEvent(
                ContentType.CHAPTER,
                chapter.getId(),
                authorId
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // 事件发布失败不应影响主业务流程
        }
    }
}
