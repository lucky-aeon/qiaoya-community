package org.xhy.community.interfaces.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.CourseDTO;
import org.xhy.community.application.course.service.AdminCourseAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.course.request.CreateCourseRequest;
import org.xhy.community.interfaces.course.request.UpdateCourseRequest;
import org.xhy.community.interfaces.course.request.CourseQueryRequest;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {
    
    private final AdminCourseAppService adminCourseAppService;

    public AdminCourseController(AdminCourseAppService adminCourseAppService) {
        this.adminCourseAppService = adminCourseAppService;
    }

    @PostMapping
    public ApiResponse<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        CourseDTO course = adminCourseAppService.createCourse(request, currentUserId);
        return ApiResponse.success(course);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<CourseDTO> updateCourse(@PathVariable String id, 
                                              @Valid @RequestBody UpdateCourseRequest request) {
        CourseDTO course = adminCourseAppService.updateCourse(id, request);
        return ApiResponse.success(course);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<CourseDTO> getCourse(@PathVariable String id) {
        CourseDTO course = adminCourseAppService.getCourseById(id);
        return ApiResponse.success(course);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable String id) {
        adminCourseAppService.deleteCourse(id);
        return ApiResponse.success();
    }
    
    @GetMapping
    public ApiResponse<IPage<CourseDTO>> getCourses(CourseQueryRequest request) {
        IPage<CourseDTO> courses = adminCourseAppService.getPagedCourses(request);
        return ApiResponse.success(courses);
    }
}