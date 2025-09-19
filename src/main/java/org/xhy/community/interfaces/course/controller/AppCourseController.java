package org.xhy.community.interfaces.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.FrontCourseDTO;
import org.xhy.community.application.course.dto.FrontCourseDetailDTO;
import org.xhy.community.application.course.service.CourseAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.course.request.AppCourseQueryRequest;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

/**
 * 前台课程控制器
 * 提供面向前台用户的课程查询功能
 * @module 前台API
 */
@RestController
@RequestMapping("/api/app/courses")
public class AppCourseController {
    
    private final CourseAppService courseAppService;

    public AppCourseController(CourseAppService courseAppService) {
        this.courseAppService = courseAppService;
    }

    /**
     * 分页查询前台课程列表
     * 获取已发布的课程列表，支持关键词搜索、技术栈和标签过滤
     * 
     * @param request 前台课程查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     *                - keyword: 课程标题关键词搜索（可选）
     *                - techStack: 技术栈筛选（可选）
     *                - tags: 标签筛选（可选）
     * @return 分页课程列表，包含课程概要信息、作者信息、章节数量等
     */
    @PostMapping("/queries")
    public ApiResponse<IPage<FrontCourseDTO>> queryCourses(@Valid @RequestBody AppCourseQueryRequest request) {
        IPage<FrontCourseDTO> courses = courseAppService.queryAppCourses(request);
        return ApiResponse.success(courses);
    }

    /**
     * 根据课程ID获取课程详情
     * 获取已发布课程的详细信息，包含完整的课程信息和章节列表
     * 
     * @param id 课程ID，UUID格式
     * @return 课程详细信息，包含：
     *         - 课程基本信息（标题、描述、技术栈、标签、评分等）
     *         - 作者信息（作者名称）
     *         - 章节列表（章节标题、排序、阅读时长等）
     *         - 课程统计信息（总阅读时长等）
     */
    @GetMapping("/{id}")
    @ActivityLog(ActivityType.VIEW_COURSE)
    public ApiResponse<FrontCourseDetailDTO> getCourseDetail(@PathVariable String id) {
        FrontCourseDetailDTO courseDetail = courseAppService.getAppCourseDetail(id);
        return ApiResponse.success(courseDetail);
    }
}