package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.application.post.service.PostAppService;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.interfaces.post.request.CreatePostRequest;
import org.xhy.community.interfaces.post.request.PostQueryRequest;
import org.xhy.community.interfaces.post.request.PostStatusRequest;
import org.xhy.community.interfaces.post.request.UpdatePostRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

/**
 * 用户文章管理控制器
 * 提供已登录用户的文章创建、编辑、删除、查询等功能
 * @module 文章管理
 */
@RestController
@RequestMapping("/api/user/posts")
public class UserPostController {
    
    private final PostAppService postAppService;

    public UserPostController(PostAppService postAppService) {
        this.postAppService = postAppService;
    }
    
    /**
     * 创建新文章
     * 为当前登录用户创建一篇新文章，默认状态为草稿
     * 需要JWT令牌认证
     * 
     * @param request 创建文章请求参数
     *                - title: 文章标题，长度5-200字符
     *                - content: 文章内容，最少10个字符，支持Markdown格式
     *                - summary: 文章概要（可选），最多500字符
     *                - coverImage: 封面图片URL（可选），最多500字符
     *                - categoryId: 分类ID，必须是有效的分类UUID
     * @return 创建成功的文章信息
     */
    @PostMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_CREATE", name = "发布文章")})
    public ApiResponse<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.createPost(request, currentUserId);
        return ApiResponse.success(post.getStatus() == PostStatus.PUBLISHED ? "发布成功" : "保存草稿",post);
    }
    
    /**
     * 更新文章
     * 修改当前用户的文章内容，只有文章作者才能修改
     * 需要JWT令牌认证
     * 
     * @param id 文章ID，UUID格式
     * @param request 更新文章请求参数（与CreatePostRequest相同的字段）
     *                - title: 文章标题，长度5-200字符
     *                - content: 文章内容，最少10个字符
     *                - summary: 文章概要（可选）
     *                - coverImage: 封面图片URL（可选）
     *                - categoryId: 分类ID
     * @return 更新后的文章信息
     */
    @PutMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_EDIT_SELF", name = "编辑本人文章")})
    public ApiResponse<PostDTO> updatePost(@PathVariable String id, @Valid @RequestBody UpdatePostRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.updatePost(id, request, currentUserId);
        return ApiResponse.success("保存成功",post);
    }
    
    /**
     * 获取文章详情
     * 查看当前用户的文章详情，只能查看自己的文章
     * 需要JWT令牌认证
     * 
     * @param id 文章ID，UUID格式
     * @return 文章详细信息，包括内容、点赞数、浏览数等
     */
    @GetMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_DETAIL_SELF", name = "查看我的文章详情")})
    public ApiResponse<PostDTO> getPost(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.getPostById(id, currentUserId);
        return ApiResponse.success(post);
    }
    
    /**
     * 分页查询当前用户的文章列表
     * 获取当前登录用户创建的所有文章，支持按状态过滤和分页
     * 需要JWT令牌认证
     * 
     * @param request 文章查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     *                - status: 文章状态过滤（可选），可选值：
     *                  * DRAFT: 草稿
     *                  * PUBLISHED: 已发布
     * @return 分页文章列表，包含文章概要信息、总数、页码等
     */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_LIST_SELF", name = "我的文章列表")})
    public ApiResponse<IPage<PostDTO>> getUserPosts(@Valid PostQueryRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        IPage<PostDTO> posts = postAppService.getUserPosts(currentUserId, request);
        return ApiResponse.success(posts);
    }
    
    /**
     * 删除文章
     * 软删除当前用户的文章，只有文章作者才能删除
     * 需要JWT令牌认证
     * 
     * @param id 文章ID，UUID格式
     * @return 空响应，删除成功返回200状态码
     */
    @DeleteMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_DELETE_SELF", name = "删除本人文章")})
    public ApiResponse<Void> deletePost(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        postAppService.deletePost(id, currentUserId);
        return ApiResponse.success("删除成功");
    }
    
    /**
     * 修改文章状态
     * 在草稿和已发布状态之间切换，只有文章作者才能修改
     * 需要JWT令牌认证
     * 
     * @param id 文章ID，UUID格式
     * @param request 文章状态请求参数
     *                - status: 目标状态，可选值：
     *                  * DRAFT: 草稿（从已发布撤回为草稿）
     *                  * PUBLISHED: 已发布（从草稿发布）
     * @return 更新后的文章信息
     */
    @PatchMapping("/{id}/status")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_STATUS_CHANGE", name = "修改文章状态")})
    public ApiResponse<PostDTO> changePostStatus(@PathVariable String id, @Valid @RequestBody PostStatusRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        
        // API层进行枚举验证和转换
        PostStatus targetStatus = request.getStatus();
        if (targetStatus != PostStatus.PUBLISHED && targetStatus != PostStatus.DRAFT) {
            throw new BusinessException(
                ValidationErrorCode.PARAM_INVALID,
                "不支持的文章状态"
            );
        }
        
        PostDTO post = postAppService.changePostStatus(id, targetStatus, currentUserId);
        return ApiResponse.success("修改成功",post);
    }

    /**
     * 采纳评论（作者）
     */
    @PostMapping("/{postId}/accept/{commentId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_ACCEPT_COMMENT", name = "采纳评论")})
    public ApiResponse<PostDTO> acceptComment(@PathVariable String postId, @PathVariable String commentId) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.acceptComment(postId, commentId, currentUserId);
        return ApiResponse.success("已采纳", post);
    }

    /**
     * 撤销采纳（作者）
     */
    @DeleteMapping("/{postId}/accept/{commentId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "POST_REVOKE_ACCEPT", name = "撤销采纳")})
    public ApiResponse<PostDTO> revokeAcceptance(@PathVariable String postId, @PathVariable String commentId) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.revokeAcceptance(postId, commentId, currentUserId);
        return ApiResponse.success("已撤销采纳", post);
    }
}
