package org.xhy.community.interfaces.public_api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.PublicCourseDTO;
import org.xhy.community.application.course.dto.PublicCourseDetailDTO;
import org.xhy.community.application.course.service.CourseAppService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.course.request.AppCourseQueryRequest;

/**
 * 公开课程控制器
 * 提供公开访问的课程展示接口，无需用户认证
 * 路由前缀：/api/public/courses
 */
@RestController
@RequestMapping("/api/public/courses")
public class PublicCourseController {

    private final CourseAppService courseAppService;

    public PublicCourseController(CourseAppService courseAppService) {
        this.courseAppService = courseAppService;
    }

    /**
     * 分页查询公开课程列表
     */
    @PostMapping("/queries")
    public ApiResponse<IPage<PublicCourseDTO>> queryCourses(@Valid @RequestBody AppCourseQueryRequest request) {
        IPage<PublicCourseDTO> courses = courseAppService.queryPublicCourses(request);
        return ApiResponse.success(courses);
    }

    /**
     * 获取公开课程详情（仅支持已发布/完成的课程）
     */
    @GetMapping("/{id}")
    public ApiResponse<PublicCourseDetailDTO> getCourseDetail(@PathVariable String id) {
        PublicCourseDetailDTO courseDetail = courseAppService.getPublicCourseDetail(id);
        return ApiResponse.success(courseDetail);
    }
}
