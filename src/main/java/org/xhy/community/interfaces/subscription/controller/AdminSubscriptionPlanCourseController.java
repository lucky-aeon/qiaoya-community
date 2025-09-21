package org.xhy.community.interfaces.subscription.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.service.AdminSubscriptionPlanCourseAppService;
import org.xhy.community.application.subscription.dto.SimpleSubscriptionPlanDTO;
import org.xhy.community.application.course.dto.SimpleCourseDTO;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanCoursesRequest;

import java.util.List;

/**
 * 管理员套餐课程绑定管理控制器
 * 提供套餐与课程绑定关系的管理功能，需要管理员权限
 * @module 套餐课程绑定管理
 */
@RestController
@RequestMapping("/api/admin/subscription-plan-courses")
public class AdminSubscriptionPlanCourseController {
    
    private final AdminSubscriptionPlanCourseAppService adminSubscriptionPlanCourseAppService;
    
    public AdminSubscriptionPlanCourseController(AdminSubscriptionPlanCourseAppService adminSubscriptionPlanCourseAppService) {
        this.adminSubscriptionPlanCourseAppService = adminSubscriptionPlanCourseAppService;
    }
    
    /**
     * 获取套餐简单列表
     * 用于前端套餐选择器，返回所有套餐的基本信息
     * @return 套餐简单列表
     */
    @GetMapping("/subscription-plans")
    public ApiResponse<List<SimpleSubscriptionPlanDTO>> getSimpleSubscriptionPlans() {
        List<SimpleSubscriptionPlanDTO> subscriptionPlans = adminSubscriptionPlanCourseAppService.getSimpleSubscriptionPlans();
        return ApiResponse.success(subscriptionPlans);
    }
    
    /**
     * 获取所有课程简单列表
     * 用于前端穿梭框的可选项，返回所有课程的基本信息
     * @return 课程简单列表
     */
    @GetMapping("/courses")
    public ApiResponse<List<SimpleCourseDTO>> getSimpleCourses() {
        List<SimpleCourseDTO> courses = adminSubscriptionPlanCourseAppService.getSimpleCourses();
        return ApiResponse.success(courses);
    }
    
    /**
     * 获取套餐已绑定的课程ID列表
     * 用于初始化穿梭框右侧已选项
     * @param planId 套餐ID
     * @return 课程ID列表
     */
    @GetMapping("/{planId}/course-ids")
    public ApiResponse<List<String>> getSubscriptionPlanCourseIds(@PathVariable String planId) {
        List<String> courseIds = adminSubscriptionPlanCourseAppService.getSubscriptionPlanCourseIds(planId);
        return ApiResponse.success(courseIds);
    }
    
    /**
     * 批量更新套餐课程绑定
     * 全量替换指定套餐的课程绑定关系
     * 
     * 支持的操作：
     * - 传入课程ID列表：绑定这些课程到套餐
     * - 传入空列表 [] 或 null：清空套餐的所有课程绑定
     * 
     * @param planId 套餐ID
     * @param request 更新请求，包含课程ID列表
     * @return 空响应
     */
    @PutMapping("/{planId}")
    public ApiResponse<Void> updateSubscriptionPlanCourses(@PathVariable String planId, 
                                                         @Valid @RequestBody UpdateSubscriptionPlanCoursesRequest request) {
        adminSubscriptionPlanCourseAppService.updateSubscriptionPlanCourses(planId, request);
        return ApiResponse.success("绑定成功");
    }
}