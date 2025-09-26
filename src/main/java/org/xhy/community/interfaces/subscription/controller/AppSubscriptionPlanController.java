package org.xhy.community.interfaces.subscription.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.dto.AppSubscriptionPlanDTO;
import org.xhy.community.application.subscription.service.SubscriptionPlanAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.List;

/**
 * 前台套餐控制器
 * 提供面向前台用户的套餐查询功能
 * @module 前台API
 */
@RestController
@RequestMapping("/api/app/subscription-plans")
public class AppSubscriptionPlanController {

    private final SubscriptionPlanAppService subscriptionPlanAppService;

    public AppSubscriptionPlanController(SubscriptionPlanAppService subscriptionPlanAppService) {
        this.subscriptionPlanAppService = subscriptionPlanAppService;
    }

    /**
     * 获取可用套餐列表
     * 获取所有激活状态的套餐，按等级升序排列
     *
     * @return 可用套餐列表，包含：
     *         - id: 套餐ID
     *         - name: 套餐名称
     *         - level: 套餐等级
     *         - validityMonths: 有效期（月）
     *         - price: 套餐价格
     *         - benefits: 套餐权益列表
     */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "SUBSCRIPTION_PLAN_APP_LIST", name = "前台套餐列表")})
    public ApiResponse<List<AppSubscriptionPlanDTO>> getActiveSubscriptionPlans() {
        List<AppSubscriptionPlanDTO> plans = subscriptionPlanAppService.getActivePaidSubscriptionPlans();
        return ApiResponse.success(plans);
    }
}
