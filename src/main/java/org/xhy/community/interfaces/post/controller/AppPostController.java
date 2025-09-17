package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.FrontPostDetailDTO;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.application.post.service.PostAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.post.request.AppPostQueryRequest;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

/**
 * 用户前台文章控制器
 * 提供面向应用端用户的文章查询功能
 * @module 用户前台API
 */
@RestController
@RequestMapping("/api/app/posts")
public class AppPostController {
    
    private final PostAppService postAppService;

    public AppPostController(PostAppService postAppService) {
        this.postAppService = postAppService;
    }

    /**
     * 分页查询用户前台文章列表
     * 获取已发布的文章列表，支持按分类类型过滤和分页
     * 
     * @param request 用户前台文章查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     *                - categoryType: 分类类型过滤（可选），可选值：
     *                  * ARTICLE: 文章类型
     *                  * QA: 问答类型
     * @return 分页文章列表，包含文章概要信息、作者信息、统计数据等
     */
    @PostMapping("/queries")
    public ApiResponse<IPage<FrontPostDTO>> queryPosts(@Valid @RequestBody AppPostQueryRequest request) {
        IPage<FrontPostDTO> posts = postAppService.queryAppPosts(request);
        return ApiResponse.success(posts);
    }

    /**
     * 根据文章ID获取文章详情
     * 获取已发布文章的详细信息，包含完整内容
     * 
     * @param id 文章ID，UUID格式
     * @return 文章详细信息，包含：
     *         - 文章基本信息（标题、内容、概要、封面图等）
     *         - 作者信息（作者名称）
     *         - 分类信息（分类名称）
     *         - 统计数据（点赞数、浏览数、评论数）
     *         - 发布时间等
     */
    @GetMapping("/{id}")
    @ActivityLog(ActivityType.VIEW_POST)
    public ApiResponse<FrontPostDetailDTO> getPostDetail(@PathVariable String id) {
        FrontPostDetailDTO postDetail = postAppService.getAppPostDetail(id);
        return ApiResponse.success(postDetail);
    }
}