package org.xhy.community.interfaces.oauth.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.auth.dto.UserSocialBindStatusDTO;
import org.xhy.community.application.auth.service.GithubAuthAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.interfaces.oauth.request.GithubCallbackRequest;

@RestController
@RequestMapping("/api/user/oauth/github")
public class UserOAuthController {

    private final GithubAuthAppService githubAuthAppService;

    public UserOAuthController(GithubAuthAppService githubAuthAppService) {
        this.githubAuthAppService = githubAuthAppService;
    }

    @GetMapping("/status")
    public ApiResponse<UserSocialBindStatusDTO> status() {
        String userId = org.xhy.community.infrastructure.config.UserContext.getCurrentUserId();
        return ApiResponse.success(githubAuthAppService.getGithubBindStatus(userId));
    }

    @PostMapping("/bind")
    @ActivityLog(ActivityType.OAUTH_BIND)
    public ApiResponse<UserSocialBindStatusDTO> bind(@Valid @RequestBody GithubCallbackRequest request) {
        String userId = org.xhy.community.infrastructure.config.UserContext.getCurrentUserId();
        return ApiResponse.success(githubAuthAppService.bindGithub(userId, request.getCode(), request.getState()));
    }

    @PostMapping("/unbind")
    @ActivityLog(ActivityType.OAUTH_UNBIND)
    public ApiResponse<Void> unbind() {
        String userId = org.xhy.community.infrastructure.config.UserContext.getCurrentUserId();
        githubAuthAppService.unbindGithub(userId);
        return ApiResponse.success("解绑成功");
    }
}
