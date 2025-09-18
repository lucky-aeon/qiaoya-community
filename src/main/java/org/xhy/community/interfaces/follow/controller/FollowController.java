package org.xhy.community.interfaces.follow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.follow.dto.FollowDTO;
import org.xhy.community.application.follow.dto.FollowStatisticsDTO;
import org.xhy.community.application.follow.service.FollowAppService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.infrastructure.annotation.LogUserActivity;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.follow.request.CreateFollowRequest;
import org.xhy.community.interfaces.follow.request.FollowQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * 关注控制器
 * 处理用户关注相关的API请求
 */
@RestController
@RequestMapping("/api/follows")
public class FollowController {
    
    private final FollowAppService followAppService;
    
    public FollowController(FollowAppService followAppService) {
        this.followAppService = followAppService;
    }
    
    /**
     * 创建关注
     */
    @PostMapping
    public ApiResponse<FollowDTO> createFollow(@Valid @RequestBody CreateFollowRequest request) {
        FollowDTO follow = followAppService.createFollow(request.getTargetId(), request.getTargetType());
        return ApiResponse.success(follow);
    }
    
    /**
     * 取消关注
     */
    @DeleteMapping("/{targetType}/{targetId}")
    public ApiResponse<Void> unfollow(@PathVariable FollowTargetType targetType,
                                     @PathVariable String targetId) {
        followAppService.unfollow(targetId, targetType);
        return ApiResponse.success();
    }
    
    /**
     * 检查关注状态
     */
    @GetMapping("/check/{targetType}/{targetId}")
    public ApiResponse<Boolean> checkFollowStatus(@PathVariable FollowTargetType targetType,
                                                 @PathVariable String targetId) {
        boolean isFollowing = followAppService.checkFollowStatus(targetId, targetType);
        return ApiResponse.success(isFollowing);
    }
    
    /**
     * 获取我的关注列表
     */
    @GetMapping("/my")
    public ApiResponse<IPage<FollowDTO>> getMyFollowings(FollowQueryRequest request) {
        IPage<FollowDTO> followings = followAppService.getMyFollowings(
            request.getTargetType(), 
            request.getPageNum(), 
            request.getPageSize()
        );
        return ApiResponse.success(followings);
    }
    
    /**
     * 获取目标对象的关注者列表
     */
    @GetMapping("/followers/{targetType}/{targetId}")
    public ApiResponse<IPage<FollowDTO>> getFollowers(@PathVariable FollowTargetType targetType,
                                                     @PathVariable String targetId,
                                                     FollowQueryRequest request) {
        IPage<FollowDTO> followers = followAppService.getFollowers(
            targetId, 
            targetType, 
            request.getPageNum(), 
            request.getPageSize()
        );
        return ApiResponse.success(followers);
    }
    
    /**
     * 获取关注统计信息
     */
    @GetMapping("/stats/{targetType}/{targetId}")
    public ApiResponse<FollowStatisticsDTO> getFollowStatistics(@PathVariable FollowTargetType targetType,
                                                               @PathVariable String targetId) {
        FollowStatisticsDTO statistics = followAppService.getFollowStatistics(targetId, targetType);
        return ApiResponse.success(statistics);
    }
    
    /**
     * 获取我的关注统计信息
     */
    @GetMapping("/my/stats")
    public ApiResponse<FollowStatisticsDTO> getMyFollowStatistics() {
        FollowStatisticsDTO statistics = followAppService.getUserFollowStatistics(null);
        return ApiResponse.success(statistics);
    }
    
    /**
     * 批量检查关注状态
     */
    @PostMapping("/check/batch")
    public ApiResponse<Map<String, Boolean>> batchCheckFollowStatus(
            @RequestParam FollowTargetType targetType,
            @RequestBody List<String> targetIds) {
        Map<String, Boolean> statusMap = followAppService.batchCheckFollowStatus(targetIds, targetType);
        return ApiResponse.success(statusMap);
    }
}