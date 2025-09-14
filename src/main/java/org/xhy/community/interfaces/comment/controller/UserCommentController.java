package org.xhy.community.interfaces.comment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.application.comment.service.UserCommentAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.comment.request.CreateReplyCommentRequest;
import org.xhy.community.interfaces.comment.request.CommentQueryRequest;
import org.xhy.community.interfaces.comment.request.CreateCommentRequest;

/**
 * 用户评论管理控制器
 * 提供用户的评论、回复、删除和查询功能
 * @module 评论管理
 */
@RestController
@RequestMapping("/api/user/comments")
public class UserCommentController {
    
    private final UserCommentAppService userCommentAppService;
    
    public UserCommentController(UserCommentAppService userCommentAppService) {
        this.userCommentAppService = userCommentAppService;
    }
    
    /**
     * 创建评论
     * 用户对文章或其他内容进行评论
     * 需要JWT令牌认证
     * 
     * @param request 创建评论请求参数，包含评论内容和目标对象
     * @return 创建成功的评论详情信息
     */
    @PostMapping
    public ApiResponse<CommentDTO> createComment(@Valid @RequestBody CreateCommentRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        
        CommentDTO comment = userCommentAppService.createComment(request, currentUserId);
        return ApiResponse.success(comment);
    }
    
    /**
     * 回复评论
     * 用户对已有评论进行回复操作
     * 需要JWT令牌认证
     * 
     * @param commentId 父评论ID，UUID格式
     * @param request 回复评论请求参数，包含回复内容
     * @return 创建成功的回复评论信息
     */
    @PostMapping("/{commentId}/reply")
    public ApiResponse<CommentDTO> replyComment(@PathVariable String commentId,
                                               @Valid @RequestBody CreateReplyCommentRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        request.setParentCommentId(commentId);
        
        CommentDTO comment = userCommentAppService.replyComment(request, currentUserId);
        return ApiResponse.success(comment);
    }
    
    /**
     * 删除评论
     * 用户删除自己的评论，只有评论作者才能操作
     * 需要JWT令牌认证
     * 
     * @param commentId 评论ID，UUID格式
     * @return 删除操作结果
     */
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        String currentUserId = UserContext.getCurrentUserId();
        userCommentAppService.deleteComment(commentId, currentUserId);
        return ApiResponse.success();
    }
    
    /**
     * 获取用户相关评论
     * 分页查询当前用户的相关评论，包括用户发表的评论和收到的回复
     * 需要JWT令牌认证
     * 
     * @param request 评论查询请求参数，包含分页和筛选条件
     * @return 分页的用户相关评论数据
     */
    @GetMapping("/related")
    public ApiResponse<IPage<CommentDTO>> getUserRelatedComments(CommentQueryRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        IPage<CommentDTO> comments = userCommentAppService.getUserRelatedComments(request, currentUserId);
        return ApiResponse.success(comments);
    }
}