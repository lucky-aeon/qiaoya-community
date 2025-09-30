package org.xhy.community.interfaces.like.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.like.dto.LikeCountDTO;
import org.xhy.community.application.like.dto.LikeStatusDTO;
import org.xhy.community.application.like.service.LikeAppService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.like.request.BatchLikeRequest;
import org.xhy.community.interfaces.like.request.ToggleLikeRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点赞控制器
 * 提供点赞相关功能
 * @module 点赞管理
 */
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeAppService likeAppService;

    public LikeController(LikeAppService likeAppService) {
        this.likeAppService = likeAppService;
    }

    /**
     * 切换点赞状态
     * 未点赞->点赞，已点赞->取消点赞
     * 需要JWT令牌认证
     *
     * @param request 点赞切换请求参数
     *                - targetId: 目标对象ID
     *                - targetType: 目标类型（COURSE/POST/CHAPTER/COMMENT）
     * @return 切换后的点赞状态
     */
    @PostMapping("/toggle")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LIKE_TOGGLE", name = "点赞/取消点赞")})
    public ApiResponse<Map<String, Object>> toggleLike(@Valid @RequestBody ToggleLikeRequest request) {
        boolean isLiked = likeAppService.toggleLike(request.getTargetId(), request.getTargetType());

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);

        String message = isLiked ? "点赞成功" : "取消点赞成功";
        return ApiResponse.success(message, result);
    }

    /**
     * 查询点赞状态
     * 查询当前用户是否已点赞指定目标
     * 需要JWT令牌认证
     *
     * @param targetType 目标类型
     * @param targetId 目标对象ID
     * @return 点赞状态信息
     */
    @GetMapping("/status/{targetType}/{targetId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LIKE_STATUS", name = "查询点赞状态")})
    public ApiResponse<LikeStatusDTO> getLikeStatus(@PathVariable LikeTargetType targetType,
                                                     @PathVariable String targetId) {
        LikeStatusDTO status = likeAppService.getLikeStatus(targetId, targetType);
        return ApiResponse.success(status);
    }

    /**
     * 批量查询点赞状态
     * 需要JWT令牌认证
     *
     * @param request 批量查询请求，包含多个目标
     * @return 点赞状态列表
     */
    @PostMapping("/status/batch")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LIKE_STATUS_BATCH", name = "批量点赞状态")})
    public ApiResponse<List<LikeStatusDTO>> batchGetLikeStatus(@Valid @RequestBody BatchLikeRequest request) {
        Map<String, LikeTargetType> targets = request.getTargets().stream()
                .collect(Collectors.toMap(
                        BatchLikeRequest.LikeTargetItem::getTargetId,
                        BatchLikeRequest.LikeTargetItem::getTargetType
                ));

        List<LikeStatusDTO> statusList = likeAppService.batchGetLikeStatus(targets);
        return ApiResponse.success(statusList);
    }

    /**
     * 统计点赞数
     * 查询指定目标的点赞总数
     *
     * @param targetType 目标类型
     * @param targetId 目标对象ID
     * @return 点赞统计信息
     */
    @GetMapping("/count/{targetType}/{targetId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LIKE_COUNT", name = "点赞计数")})
    public ApiResponse<LikeCountDTO> getLikeCount(@PathVariable LikeTargetType targetType,
                                                   @PathVariable String targetId) {
        LikeCountDTO count = likeAppService.getLikeCount(targetId, targetType);
        return ApiResponse.success(count);
    }

    /**
     * 批量统计点赞数
     *
     * @param request 批量查询请求，包含多个目标
     * @return 点赞统计列表
     */
    @PostMapping("/count/batch")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LIKE_COUNT_BATCH", name = "批量点赞计数")})
    public ApiResponse<List<LikeCountDTO>> batchGetLikeCount(@Valid @RequestBody BatchLikeRequest request) {
        Map<String, LikeTargetType> targets = request.getTargets().stream()
                .collect(Collectors.toMap(
                        BatchLikeRequest.LikeTargetItem::getTargetId,
                        BatchLikeRequest.LikeTargetItem::getTargetType
                ));

        List<LikeCountDTO> countList = likeAppService.batchGetLikeCount(targets);
        return ApiResponse.success(countList);
    }
}
