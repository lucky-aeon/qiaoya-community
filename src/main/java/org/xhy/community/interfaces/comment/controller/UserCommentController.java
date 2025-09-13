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

@RestController
@RequestMapping("/api/user/comments")
public class UserCommentController {
    
    private final UserCommentAppService userCommentAppService;
    
    public UserCommentController(UserCommentAppService userCommentAppService) {
        this.userCommentAppService = userCommentAppService;
    }
    
    @PostMapping
    public ApiResponse<CommentDTO> createComment(@Valid @RequestBody CreateCommentRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        
        CommentDTO comment = userCommentAppService.createComment(request, currentUserId);
        return ApiResponse.success(comment);
    }
    
    @PostMapping("/{commentId}/reply")
    public ApiResponse<CommentDTO> replyComment(@PathVariable String commentId,
                                               @Valid @RequestBody CreateReplyCommentRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        request.setParentCommentId(commentId);
        
        CommentDTO comment = userCommentAppService.replyComment(request, currentUserId);
        return ApiResponse.success(comment);
    }
    
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        String currentUserId = UserContext.getCurrentUserId();
        userCommentAppService.deleteComment(commentId, currentUserId);
        return ApiResponse.success();
    }
    
    @GetMapping("/related")
    public ApiResponse<IPage<CommentDTO>> getUserRelatedComments(CommentQueryRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        IPage<CommentDTO> comments = userCommentAppService.getUserRelatedComments(request, currentUserId);
        return ApiResponse.success(comments);
    }
}