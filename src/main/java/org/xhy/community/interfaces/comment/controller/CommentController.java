package org.xhy.community.interfaces.comment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.application.comment.service.UserCommentAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.comment.request.BusinessCommentQueryRequest;

/**
 * 评论查询控制器
 * 提供业务评论的公开查询功能，无需登录认证
 * @module 评论管理
 */
@RestController
@RequestMapping("/api/app/comments")
public class CommentController {
    
    private final UserCommentAppService userCommentAppService;
    
    public CommentController(UserCommentAppService userCommentAppService) {
        this.userCommentAppService = userCommentAppService;
    }
    
    /**
     * 查询业务评论列表
     * 获取指定业务对象（文章、课程等）的评论列表
     * 无需认证，公开访问
     * 
     * @param request 业务评论查询请求参数
     * @return 分页评论列表
     */
    @GetMapping
    public ApiResponse<IPage<CommentDTO>> getBusinessComments(@Valid BusinessCommentQueryRequest request) {
        IPage<CommentDTO> comments = userCommentAppService.getBusinessComments(request);
        return ApiResponse.success(comments);
    }
}