package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.repository.CourseRepository;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.application.course.dto.SimpleCourseDTO;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;

import java.math.BigDecimal;
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
    
    public IPage<CourseEntity> getPagedCourses(int pageNum, int pageSize) {
        Page<CourseEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<CourseEntity>()
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
}