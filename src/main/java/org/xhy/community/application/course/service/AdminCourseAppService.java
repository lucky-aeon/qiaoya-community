package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.application.course.dto.CourseDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.interfaces.course.request.CreateCourseRequest;
import org.xhy.community.interfaces.course.request.UpdateCourseRequest;
import org.xhy.community.interfaces.course.request.CourseQueryRequest;

import java.math.BigDecimal;

@Service
public class AdminCourseAppService {
    
    private final CourseDomainService courseDomainService;
    
    public AdminCourseAppService(CourseDomainService courseDomainService) {
        this.courseDomainService = courseDomainService;
    }
    
    public CourseDTO createCourse(CreateCourseRequest request, String authorId) {
        CourseEntity course = CourseAssembler.fromCreateRequest(request, authorId);
        
        CourseEntity createdCourse = courseDomainService.createCourse(course);
        
        return CourseAssembler.toDTO(createdCourse);
    }
    
    public CourseDTO updateCourse(String courseId, UpdateCourseRequest request) {
        CourseEntity course = CourseAssembler.fromUpdateRequest(request, courseId);
        
        CourseEntity updatedCourse = courseDomainService.updateCourse(course);
        
        return CourseAssembler.toDTO(updatedCourse);
    }

    public void deleteCourse(String courseId) {
        courseDomainService.deleteCourse(courseId);
    }
    
    public CourseDTO getCourseById(String courseId) {
        CourseEntity course = courseDomainService.getCourseById(courseId);
        return CourseAssembler.toDTO(course);
    }
    
    public IPage<CourseDTO> getPagedCourses(CourseQueryRequest request) {
        IPage<CourseEntity> coursePage;

        coursePage = courseDomainService.getPagedCourses(
                request.getPageNum(),
                request.getPageSize()
        );
        
        return coursePage.convert(CourseAssembler::toDTO);
    }
}