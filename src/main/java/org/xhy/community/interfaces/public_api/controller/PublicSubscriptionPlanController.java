package org.xhy.community.interfaces.public_api.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.dto.AppSubscriptionPlanDTO;
import org.xhy.community.application.subscription.service.SubscriptionPlanAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

/**
 * 对外套餐控制器
 * 提供面向外部系统的付费套餐查询功能
 * @module 对外API
 */
@RestController
@RequestMapping("/api/public/subscription-plans")
public class PublicSubscriptionPlanController {

    private final SubscriptionPlanAppService subscriptionPlanAppService;

    public PublicSubscriptionPlanController(SubscriptionPlanAppService subscriptionPlanAppService) {
        this.subscriptionPlanAppService = subscriptionPlanAppService;
    }

    /**
     * 获取可用付费套餐列表
     * 获取所有激活状态且价格大于0的套餐，按等级升序排列
     *
     * @return 可用付费套餐列表，包含：
     *         - id: 套餐ID
     *         - name: 套餐名称
     *         - level: 套餐等级
     *         - validityMonths: 有效期（月）
     *         - price: 套餐价格（排除0元套餐）
     *         - benefits: 套餐权益列表
     */
    @GetMapping
    public ApiResponse<List<AppSubscriptionPlanDTO>> getActivePaidSubscriptionPlans() {
        List<AppSubscriptionPlanDTO> plans = subscriptionPlanAppService.getActivePaidSubscriptionPlans();
        return ApiResponse.success(plans);
    }
}