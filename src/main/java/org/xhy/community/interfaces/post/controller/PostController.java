package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/user/posts")
public class PostController {
    
    @Autowired
    private PostAppService postAppService;
    
    @PostMapping
    public ApiResponse<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.createPost(request, currentUserId);
        return ApiResponse.success(post);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<PostDTO> updatePost(@PathVariable String id, @Valid @RequestBody UpdatePostRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.updatePost(id, request, currentUserId);
        return ApiResponse.success(post);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<PostDTO> getPost(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        PostDTO post = postAppService.getPostById(id, currentUserId);
        return ApiResponse.success(post);
    }
    
    @GetMapping
    public ApiResponse<IPage<PostDTO>> getUserPosts(@Valid PostQueryRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        IPage<PostDTO> posts = postAppService.getUserPosts(currentUserId, request);
        return ApiResponse.success(posts);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        postAppService.deletePost(id, currentUserId);
        return ApiResponse.success();
    }
    
    @PatchMapping("/{id}/status")
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
        return ApiResponse.success(post);
    }
}