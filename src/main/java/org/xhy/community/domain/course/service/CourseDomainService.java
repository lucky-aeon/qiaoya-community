package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.repository.CourseRepository;
import org.xhy.community.domain.common.event.ContentPublishedEvent;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;
import org.xhy.community.domain.course.query.CourseQuery;
import org.xhy.community.domain.course.valueobject.CourseStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseDomainService {
    private static final Logger log = LoggerFactory.getLogger(CourseDomainService.class);

    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CourseDomainService(CourseRepository courseRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.courseRepository = courseRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public CourseEntity createCourse(CourseEntity course) {
        courseRepository.insert(course);

        // 发布简化的课程创建事件
        publishContentEvent(course);
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
            log.warn("【课程】未找到：courseId={}", courseId);
            throw new BusinessException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }
    
    public IPage<CourseEntity> getPagedCourses(CourseQuery query) {
        Page<CourseEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<CourseEntity>()
                .like(StringUtils.hasText(query.getTitle()), CourseEntity::getTitle, query.getTitle())
                .orderByDesc(CourseEntity::getSortOrder)
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
                .eq(CourseEntity::getStatus, CourseStatus.COMPLETED)
                .like(StringUtils.hasText(query.getTitle()), CourseEntity::getTitle, query.getTitle())
                .orderByDesc(CourseEntity::getSortOrder)
                .orderByDesc(CourseEntity::getCreateTime);
        
        return courseRepository.selectPage(page, queryWrapper);
    }
    
    /**
     * 获取所有课程实体，按创建时间倒序。
     * 分层约束：仅返回领域实体，DTO 转换在 Application 层完成。
     */
    public List<CourseEntity> getAllCourses() {
        return courseRepository.selectList(
            new LambdaQueryWrapper<CourseEntity>()
                .orderByDesc(CourseEntity::getSortOrder)
                .orderByDesc(CourseEntity::getCreateTime)
        );
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

    /**
     * 发布简化的课程内容事件
     * 只包含必要的标识信息，由Application层统一处理通知逻辑
     */
    private void publishContentEvent(CourseEntity course) {
        try {
            ContentPublishedEvent event = new ContentPublishedEvent(
                ContentType.COURSE,
                course.getId(),
                course.getAuthorId()
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // 事件发布失败不应影响主业务流程
        }
    }

    // ==================== 统计方法 ====================

    /**
     * 获取课程列表（用于统计）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 课程列表
     */
    public List<CourseEntity> getCourses(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<CourseEntity>()
                .ge(startTime != null, CourseEntity::getCreateTime, startTime)
                .le(endTime != null, CourseEntity::getCreateTime, endTime)
                .orderByAsc(CourseEntity::getCreateTime);

        return courseRepository.selectList(queryWrapper);
    }
}
