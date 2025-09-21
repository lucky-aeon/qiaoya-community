package org.xhy.community.interfaces.subscription.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.dto.SubscriptionPlanDTO;
import org.xhy.community.application.subscription.service.AdminSubscriptionPlanAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.subscription.request.CreateSubscriptionPlanRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionPlanQueryRequest;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanRequest;

/**
 * 管理员套餐管理控制器
 * 提供套餐的增删改查等管理功能，需要管理员权限
 * @module 套餐管理
 */
@RestController
@RequestMapping("/api/admin/subscription-plans")
public class AdminSubscriptionPlanController {
    
    private final AdminSubscriptionPlanAppService adminSubscriptionPlanAppService;
    
    public AdminSubscriptionPlanController(AdminSubscriptionPlanAppService adminSubscriptionPlanAppService) {
        this.adminSubscriptionPlanAppService = adminSubscriptionPlanAppService;
    }
    
    /**
     * 创建新套餐
     * 管理员创建新的套餐
     * @param request 创建套餐请求参数
     * @return 创建成功的套餐信息
     */
    @PostMapping
    public ApiResponse<SubscriptionPlanDTO> createSubscriptionPlan(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        SubscriptionPlanDTO subscriptionPlan = adminSubscriptionPlanAppService.createSubscriptionPlan(request);
        return ApiResponse.success("创建成功",subscriptionPlan);
    }
    
    /**
     * 更新套餐信息
     * 管理员更新套餐的基本信息
     * @param id 套餐ID
     * @param request 更新套餐请求参数
     * @return 更新后的套餐信息
     */
    @PutMapping("/{id}")
    public ApiResponse<SubscriptionPlanDTO> updateSubscriptionPlan(@PathVariable String id, 
                                                                 @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        SubscriptionPlanDTO subscriptionPlan = adminSubscriptionPlanAppService.updateSubscriptionPlan(id, request);
        return ApiResponse.success("更新成功",subscriptionPlan);
    }
    
    /**
     * 获取套餐详情
     * 查看套餐的详细信息
     * @param id 套餐ID
     * @return 套餐详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SubscriptionPlanDTO> getSubscriptionPlan(@PathVariable String id) {
        SubscriptionPlanDTO subscriptionPlan = adminSubscriptionPlanAppService.getSubscriptionPlanById(id);
        return ApiResponse.success(subscriptionPlan);
    }
    
    /**
     * 删除套餐
     * 管理员删除套餐（软删除）
     * @param id 套餐ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSubscriptionPlan(@PathVariable String id) {
        adminSubscriptionPlanAppService.deleteSubscriptionPlan(id);
        return ApiResponse.success("删除成功");
    }
    
    /**
     * 分页获取套餐列表
     * 支持按名称和级别筛选的分页查询
     * @param request 查询请求参数
     * @return 分页套餐列表
     */
    @GetMapping
    public ApiResponse<IPage<SubscriptionPlanDTO>> getSubscriptionPlans(SubscriptionPlanQueryRequest request) {
        IPage<SubscriptionPlanDTO> subscriptionPlans = adminSubscriptionPlanAppService.getPagedSubscriptionPlans(request);
        return ApiResponse.success(subscriptionPlans);
    }
}