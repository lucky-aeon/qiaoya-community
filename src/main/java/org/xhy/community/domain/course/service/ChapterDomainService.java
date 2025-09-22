package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.repository.ChapterRepository;
import org.xhy.community.domain.course.repository.CourseRepository;
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
    
    public ChapterDomainService(ChapterRepository chapterRepository, CourseRepository courseRepository) {
        this.chapterRepository = chapterRepository;
        this.courseRepository = courseRepository;
    }
    
    public ChapterEntity createChapter(ChapterEntity chapter) {
        CourseEntity course = courseRepository.selectById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        
        chapterRepository.insert(chapter);
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
}