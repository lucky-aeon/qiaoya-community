package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.application.post.service.PostAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.post.request.PublicPostQueryRequest;

/**
 * 公开文章查询控制器
 * 提供公开的文章查询功能，无需登录认证
 * @module 公开API
 */
@RestController
@RequestMapping("/api/public/posts")
public class PublicPostController {
    
    private final PostAppService postAppService;

    public PublicPostController(PostAppService postAppService) {
        this.postAppService = postAppService;
    }

    /**
     * 分页查询公开文章列表
     * 获取已发布的文章列表，支持按分类类型过滤和分页
     * 无需认证，公开访问
     * 
     * @param request 公开文章查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     *                - categoryType: 分类类型过滤（可选），可选值：
     *                  * ARTICLE: 文章类型
     *                  * QA: 问答类型
     * @return 分页文章列表，包含文章概要信息、作者信息、统计数据等
     */
    @PostMapping("/queries")
    public ApiResponse<IPage<FrontPostDTO>> queryPosts(@Valid @RequestBody PublicPostQueryRequest request) {
        IPage<FrontPostDTO> posts = postAppService.queryPublicPosts(request);
        return ApiResponse.success(posts);
    }
}