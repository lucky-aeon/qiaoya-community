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

/**
 * 管理员课程管理控制器
 * 提供课程的增删改查等管理功能，需要管理员权限
 * @module 课程管理
 */
@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {
    
    private final AdminCourseAppService adminCourseAppService;

    public AdminCourseController(AdminCourseAppService adminCourseAppService) {
        this.adminCourseAppService = adminCourseAppService;
    }

    /**
     * 创建新课程
     * 管理员创建新的课程，需要管理员权限
     * @param request 创建课程请求参数
     * @return 创建成功的课程信息
     */
    @PostMapping
    public ApiResponse<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        CourseDTO course = adminCourseAppService.createCourse(request, currentUserId);
        return ApiResponse.success(course);
    }
    
    /**
     * 更新课程信息
     * 管理员更新课程的基本信息
     * @param id 课程ID
     * @param request 更新课程请求参数
     * @return 更新后的课程信息
     */
    @PutMapping("/{id}")
    public ApiResponse<CourseDTO> updateCourse(@PathVariable String id, 
                                              @Valid @RequestBody UpdateCourseRequest request) {
        CourseDTO course = adminCourseAppService.updateCourse(id, request);
        return ApiResponse.success(course);
    }
    
    /**
     * 获取课程详情
     * 查看课程的详细信息
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CourseDTO> getCourse(@PathVariable String id) {
        CourseDTO course = adminCourseAppService.getCourseById(id);
        return ApiResponse.success(course);
    }
    
    /**
     * 删除课程
     * 管理员删除课程（软删除）
     * @param id 课程ID
     * @return 空响应
     */
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