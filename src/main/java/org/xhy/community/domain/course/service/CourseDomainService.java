package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.repository.CourseRepository;
import org.xhy.community.application.course.dto.SimpleCourseDTO;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;
import org.xhy.community.domain.course.query.CourseQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseDomainService {
    
    private final CourseRepository courseRepository;
    
    public CourseDomainService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    
    public CourseEntity createCourse(CourseEntity course) {
        courseRepository.insert(course);
        return course;
    }
    
    public CourseEntity updateCourse(CourseEntity course) {
        courseRepository.updateById(course);
        return course;
    }
    
    public void deleteCourse(String courseId) {
        getCourseById(courseId);
        courseRepository.deleteById(courseId);
    }
    
    public CourseEntity getCourseById(String courseId) {
        CourseEntity course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new BusinessException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }
    
    public IPage<CourseEntity> getPagedCourses(CourseQuery query) {
        Page<CourseEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<CourseEntity>()
                .like(StringUtils.hasText(query.getTitle()), CourseEntity::getTitle, query.getTitle())
                .eq(query.getIsPublished() != null, CourseEntity::getStatus, 
                    query.getIsPublished() != null && query.getIsPublished() ? 
                    org.xhy.community.domain.course.valueobject.CourseStatus.COMPLETED : null)
                .orderByDesc(CourseEntity::getCreateTime);
        
        return courseRepository.selectPage(page, queryWrapper);
    }
    
    /**
     * 分页查询已发布的课程
     * 专门用于前台查询，只返回已完成状态的课程
     *
     * @param query 查询条件
     * @return 已发布课程的分页结果
     */
    public IPage<CourseEntity> getPublishedPagedCourses(CourseQuery query) {
        Page<CourseEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<CourseEntity>()
                .eq(CourseEntity::getStatus, org.xhy.community.domain.course.valueobject.CourseStatus.COMPLETED)
                .like(StringUtils.hasText(query.getTitle()), CourseEntity::getTitle, query.getTitle())
                .orderByDesc(CourseEntity::getCreateTime);
        
        return courseRepository.selectPage(page, queryWrapper);
    }
    
    public List<SimpleCourseDTO> getAllSimpleCourses() {
        List<CourseEntity> entities = courseRepository.selectList(
            new LambdaQueryWrapper<CourseEntity>()
                .orderByDesc(CourseEntity::getCreateTime)
        );
        return entities.stream()
                      .map(CourseAssembler::toSimpleDTO)
                      .collect(Collectors.toList());
    }
    
    /**
     * 批量获取课程标题映射
     * 
     * @param courseIds 课程ID集合
     * @return 课程ID到标题的映射
     */
    public Map<String, String> getCourseTitleMapByIds(Collection<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }
        
        List<CourseEntity> courses = courseRepository.selectBatchIds(courseIds);
        return courses.stream()
                .collect(Collectors.toMap(
                    CourseEntity::getId,
                    CourseEntity::getTitle
                ));
    }
}