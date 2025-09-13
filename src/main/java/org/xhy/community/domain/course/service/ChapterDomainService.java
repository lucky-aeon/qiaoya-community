package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.repository.ChapterRepository;
import org.xhy.community.domain.course.repository.CourseRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;

import java.util.List;

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
    
    public IPage<ChapterEntity> getPagedChapters(Integer pageNum, Integer pageSize, String courseId) {
        Page<ChapterEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ChapterEntity> queryWrapper = new LambdaQueryWrapper<ChapterEntity>()
            .eq(ChapterEntity::getDeleted, false)
            .orderByDesc(ChapterEntity::getCreateTime);
        
        if (courseId != null && !courseId.trim().isEmpty()) {
            queryWrapper.eq(ChapterEntity::getCourseId, courseId);
        }
        
        return chapterRepository.selectPage(page, queryWrapper);
    }
}