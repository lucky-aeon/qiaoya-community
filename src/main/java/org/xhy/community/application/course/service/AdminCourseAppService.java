package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.application.course.dto.CourseDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.query.CourseQuery;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.application.like.helper.LikeCountHelper;
import org.xhy.community.interfaces.course.request.CreateCourseRequest;
import org.xhy.community.interfaces.course.request.UpdateCourseRequest;
import org.xhy.community.interfaces.course.request.CourseQueryRequest;

import java.math.BigDecimal;

@Service
public class AdminCourseAppService {
    
    private final CourseDomainService courseDomainService;
    private final LikeDomainService likeDomainService;
    
    public AdminCourseAppService(CourseDomainService courseDomainService,
                                 LikeDomainService likeDomainService) {
        this.courseDomainService = courseDomainService;
        this.likeDomainService = likeDomainService;
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
        CourseDTO dto = CourseAssembler.toDTO(course);
        dto.setLikeCount(LikeCountHelper.getLikeCount(courseId, LikeTargetType.COURSE, likeDomainService));
        return dto;
    }
    
    public IPage<CourseDTO> getPagedCourses(CourseQueryRequest request) {
        CourseQuery query = CourseAssembler.fromPageRequest(request.getPageNum(), request.getPageSize());
        IPage<CourseEntity> coursePage = courseDomainService.getPagedCourses(query);

        IPage<CourseDTO> dtoPage = coursePage.convert(CourseAssembler::toDTO);
        if (dtoPage.getRecords() != null && !dtoPage.getRecords().isEmpty()) {
            LikeCountHelper.fillLikeCount(dtoPage.getRecords(), CourseDTO::getId, LikeTargetType.COURSE, CourseDTO::setLikeCount, likeDomainService);
        }
        return dtoPage;
    }
}
