package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.post.dto.AdminPostDTO;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.application.post.service.AdminPostAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.post.request.AdminPostQueryRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

/**
 * 管理员文章管理控制器
 * 提供管理员对所有文章的查询功能
 * @module 文章管理
 */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {
    
    private final AdminPostAppService adminPostAppService;
    
    public AdminPostController(AdminPostAppService adminPostAppService) {
        this.adminPostAppService = adminPostAppService;
    }
    
    /**
     * 分页查询所有文章列表
     * 管理员查看所有用户的文章，包含作者名称和分类名称
     * 需要管理员权限认证
     * 
     * @param request 分页查询请求参数
     * @return 包含完整信息的文章分页列表
     */
    @GetMapping
    public ApiResponse<IPage<AdminPostDTO>> getAdminPosts(AdminPostQueryRequest request) {
        IPage<AdminPostDTO> posts = adminPostAppService.getAdminPosts(request);
        return ApiResponse.success(posts);
    }

    /**
     * 管理员强制采纳评论
     */
    @PostMapping("/{postId}/accept/{commentId}")
    @ActivityLog(ActivityType.ADMIN_POST_FORCE_ACCEPT)
    public ApiResponse<PostDTO> forceAccept(@PathVariable String postId, @PathVariable String commentId) {
        String adminId = UserContext.getCurrentUserId();
        PostDTO post = adminPostAppService.forceAcceptComment(postId, commentId, adminId);
        return ApiResponse.success("已强制采纳", post);
    }

    /**
     * 管理员强制撤销采纳
     */
    @DeleteMapping("/{postId}/accept/{commentId}")
    @ActivityLog(ActivityType.ADMIN_POST_FORCE_REVOKE)
    public ApiResponse<PostDTO> forceRevoke(@PathVariable String postId, @PathVariable String commentId) {
        String adminId = UserContext.getCurrentUserId();
        PostDTO post = adminPostAppService.forceRevokeAcceptance(postId, commentId, adminId);
        return ApiResponse.success("已强制撤销采纳", post);
    }
}
