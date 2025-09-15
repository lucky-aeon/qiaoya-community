package org.xhy.community.interfaces.subscription.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.application.subscription.service.UserSubscriptionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.subscription.request.ActivateCDKRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionQueryRequest;

/**
 * 用户订阅控制器
 * 提供CDK激活和订阅查询功能，需要用户登录
 * @module 用户订阅管理
 */
@RestController
@RequestMapping("/api/user/subscription")
public class UserSubscriptionController {
    
    private final UserSubscriptionAppService userSubscriptionAppService;
    
    public UserSubscriptionController(UserSubscriptionAppService userSubscriptionAppService) {
        this.userSubscriptionAppService = userSubscriptionAppService;
    }
    
    /**
     * CDK激活
     * 用户输入CDK码进行激活，支持套餐CDK和课程CDK
     * @param request CDK激活请求
     * @return CDK激活结果
     */
    @PostMapping("/activate-cdk")
    public ApiResponse<Void> activateCDK(@Valid @RequestBody ActivateCDKRequest request) {
        String userId = UserContext.getCurrentUserId();
        userSubscriptionAppService.activateCDK(userId, request);
        return ApiResponse.success("激活成功");
    }
    
    /**
     * 我的订阅列表
     * 分页查询用户的所有订阅记录，包含到期时间和剩余天数
     * @param request 查询请求参数
     * @return 分页订阅列表
     */
    @GetMapping("/subscriptions")
    public ApiResponse<IPage<UserSubscriptionDTO>> getSubscriptions(SubscriptionQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<UserSubscriptionDTO> subscriptions = userSubscriptionAppService.getSubscriptions(userId, request);
        return ApiResponse.success(subscriptions);
    }
    
    /**
     * 订阅详情
     * 查看特定订阅的详细信息
     * @param subscriptionId 订阅ID
     * @return 订阅详情
     */
    @GetMapping("/{subscriptionId}")
    public ApiResponse<UserSubscriptionDTO> getSubscriptionDetail(@PathVariable String subscriptionId) {
        String userId = UserContext.getCurrentUserId();
        UserSubscriptionDTO subscription = userSubscriptionAppService.getSubscriptionDetail(userId, subscriptionId);
        return ApiResponse.success(subscription);
    }
}