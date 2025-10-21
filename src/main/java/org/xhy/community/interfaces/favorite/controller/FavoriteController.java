package org.xhy.community.interfaces.favorite.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.favorite.dto.FavoriteListItemDTO;
import org.xhy.community.application.favorite.dto.FavoriteStatusDTO;
import org.xhy.community.application.favorite.service.FavoriteAppService;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.favorite.request.BatchFavoriteRequest;
import org.xhy.community.interfaces.favorite.request.MyFavoritesQueryRequest;
import org.xhy.community.interfaces.favorite.request.ToggleFavoriteRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏控制器
 * 提供收藏相关功能
 * @module 收藏管理
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteAppService favoriteAppService;

    public FavoriteController(FavoriteAppService favoriteAppService) {
        this.favoriteAppService = favoriteAppService;
    }

    /**
     * 切换收藏状态
     * 未收藏->收藏，已收藏->取消收藏
     * 需要JWT令牌认证
     *
     * @param request 收藏切换请求参数
     *                - targetId: 目标对象ID
     *                - targetType: 目标类型（POST/CHAPTER/COMMENT/INTERVIEW_QUESTION）
     * @return 切换后的收藏状态
     */
    @PostMapping("/toggle")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "FAVORITE_TOGGLE", name = "收藏/取消收藏")})
    public ApiResponse<Map<String, Object>> toggleFavorite(@Valid @RequestBody ToggleFavoriteRequest request) {
        boolean isFavorited = favoriteAppService.toggleFavorite(request.getTargetId(), request.getTargetType());

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorited", isFavorited);

        String message = isFavorited ? "收藏成功" : "取消收藏成功";
        return ApiResponse.success(message, result);
    }

    /**
     * 查询收藏状态
     * 查询当前用户是否已收藏指定目标，并返回收藏数量
     * 需要JWT令牌认证
     *
     * @param targetType 目标类型
     * @param targetId 目标对象ID
     * @return 收藏状态信息（包含是否收藏和收藏数量）
     */
    @GetMapping("/status/{targetType}/{targetId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "FAVORITE_STATUS", name = "查询收藏状态")})
    public ApiResponse<FavoriteStatusDTO> getFavoriteStatus(@PathVariable FavoriteTargetType targetType,
                                                             @PathVariable String targetId) {
        FavoriteStatusDTO status = favoriteAppService.getFavoriteStatus(targetId, targetType);
        return ApiResponse.success(status);
    }

    /**
     * 批量查询收藏状态
     * 需要JWT令牌认证
     *
     * @param request 批量查询请求，包含多个目标
     * @return 收藏状态列表（包含是否收藏和收藏数量）
     */
    @PostMapping("/status/batch")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "FAVORITE_STATUS_BATCH", name = "批量收藏状态")})
    public ApiResponse<List<FavoriteStatusDTO>> batchGetFavoriteStatus(@Valid @RequestBody BatchFavoriteRequest request) {
        Map<String, FavoriteTargetType> targets = request.getTargets().stream()
                .collect(Collectors.toMap(
                        BatchFavoriteRequest.FavoriteTargetItem::getTargetId,
                        BatchFavoriteRequest.FavoriteTargetItem::getTargetType
                ));

        List<FavoriteStatusDTO> statusList = favoriteAppService.batchGetFavoriteStatus(targets);
        return ApiResponse.success(statusList);
    }

    /**
     * 我的收藏列表（分页）
     * 查询当前用户的收藏列表，按收藏时间倒序
     * 需要JWT令牌认证
     *
     * @param request 我的收藏查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10
     *                - targetType: 目标类型（可选），可选值：
     *                  * POST: 文章
     *                  * CHAPTER: 章节
     *                  * COMMENT: 评论
     *                  * INTERVIEW_QUESTION: 题目
     * @return 我的收藏列表（分页），每项包含：
     *         - id: 收藏ID
     *         - targetId: 目标ID
     *         - targetType: 目标类型
     *         - title: 标题
     *         - snippet: 摘要/内容片段
     *         - authorId: 作者ID
     *         - authorName: 作者名称
     *         - createTime: 收藏时间
     *         - businessId: 业务对象ID（仅COMMENT类型有值，表示评论所属的文章/课程等）
     *         - businessType: 业务对象类型（仅COMMENT类型有值，如POST/COURSE/CHAPTER等）
     */
    @GetMapping("/my")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "FAVORITE_MY_LIST", name = "我的收藏列表")})
    public ApiResponse<IPage<FavoriteListItemDTO>> getMyFavorites(@Valid MyFavoritesQueryRequest request) {
        IPage<FavoriteListItemDTO> favoritesPage = favoriteAppService.pageMyFavorites(request);
        return ApiResponse.success(favoritesPage);
    }
}
