package org.xhy.community.interfaces.comment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.application.comment.service.UserCommentAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.comment.request.BusinessCommentQueryRequest;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private final UserCommentAppService userCommentAppService;
    
    public CommentController(UserCommentAppService userCommentAppService) {
        this.userCommentAppService = userCommentAppService;
    }
    
    @GetMapping
    public ApiResponse<IPage<CommentDTO>> getBusinessComments(@Valid BusinessCommentQueryRequest request) {
        IPage<CommentDTO> comments = userCommentAppService.getBusinessComments(request);
        return ApiResponse.success(comments);
    }
}