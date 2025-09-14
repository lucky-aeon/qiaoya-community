package org.xhy.community.application.course.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.course.dto.CourseDTO;
import org.xhy.community.application.course.dto.SimpleCourseDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.interfaces.course.request.CreateCourseRequest;
import org.xhy.community.interfaces.course.request.UpdateCourseRequest;

public class CourseAssembler {
    
    public static CourseDTO toDTO(CourseEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CourseDTO dto = new CourseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static CourseEntity fromCreateRequest(CreateCourseRequest request, String authorId) {
        if (request == null) {
            return null;
        }

        CourseEntity entity = new CourseEntity();

        BeanUtils.copyProperties(request, entity);
        entity.setAuthorId(authorId);

        return entity;
    }
    
    public static CourseEntity fromUpdateRequest(CreateCourseRequest request, String courseId) {
        if (request == null) {
            return null;
        }
        
        CourseEntity entity = new CourseEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(courseId);
        
        return entity;
    }
    
    public static SimpleCourseDTO toSimpleDTO(CourseEntity entity) {
        if (entity == null) {
            return null;
        }
        
        SimpleCourseDTO dto = new SimpleCourseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}