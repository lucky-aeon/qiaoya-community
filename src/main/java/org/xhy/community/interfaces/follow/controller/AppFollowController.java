package org.xhy.community.interfaces.follow.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.follow.dto.FollowDTO;
import org.xhy.community.application.follow.service.FollowAppService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.follow.request.ToggleFollowRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 前台关注控制器
 * 提供前台页面的关注交互功能
 * @module 关注管理
 */
@RestController
@RequestMapping("/api/app/follows")
public class AppFollowController {
    
    private final FollowAppService followAppService;
    
    public AppFollowController(FollowAppService followAppService) {
        this.followAppService = followAppService;
    }
    
    /**
     * 关注/取消关注切换
     * 根据当前关注状态自动切换为关注或取消关注
     * 需要JWT令牌认证
     *
     * @param request 关注切换请求参数
     *                - targetId: 目标对象ID，UUID格式
     *                - targetType: 目标类型（USER、POST等）
     * @return 切换后的关注状态信息
     */
    @PostMapping("/toggle")
    public ApiResponse<Map<String, Object>> toggleFollow(@Valid @RequestBody ToggleFollowRequest request) {
        // 检查当前关注状态
        boolean isFollowing = followAppService.checkFollowStatus(request.getTargetId(), request.getTargetType());

        Map<String, Object> result = new HashMap<>();

        if (isFollowing) {
            // 如果已关注则取消关注
            followAppService.unfollow(request.getTargetId(), request.getTargetType());
            result.put("isFollowing", false);
            return ApiResponse.success("取消关注成功", result);
        } else {
            // 如果未关注则创建关注
            followAppService.createFollow(request.getTargetId(), request.getTargetType());
            result.put("isFollowing", true);
            return ApiResponse.success("关注成功", result);
        }
    }
    
    /**
     * 检查关注状态
     * 查询当前用户是否已关注指定目标
     * 需要JWT令牌认证
     * 
     * @param targetType 目标类型
     * @param targetId 目标对象ID
     * @return 是否已关注的状态
     */
    @GetMapping("/check/{targetType}/{targetId}")
    public ApiResponse<Boolean> checkFollowStatus(@PathVariable FollowTargetType targetType,
                                                 @PathVariable String targetId) {
        boolean isFollowing = followAppService.checkFollowStatus(targetId, targetType);
        return ApiResponse.success(isFollowing);
    }
}