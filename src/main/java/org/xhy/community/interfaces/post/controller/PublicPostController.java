package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.application.post.service.PostAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.post.request.PublicPostQueryRequest;

@RestController
@RequestMapping("/api/public/posts")
public class PublicPostController {
    
    private final PostAppService postAppService;

    public PublicPostController(PostAppService postAppService) {
        this.postAppService = postAppService;
    }

    @PostMapping("/queries")
    public ApiResponse<IPage<FrontPostDTO>> queryPosts(@Valid @RequestBody PublicPostQueryRequest request) {
        IPage<FrontPostDTO> posts = postAppService.queryPublicPosts(request);
        return ApiResponse.success(posts);
    }
}