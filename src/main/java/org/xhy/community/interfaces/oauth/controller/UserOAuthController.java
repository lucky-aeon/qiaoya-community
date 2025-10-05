package org.xhy.community.interfaces.oauth.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.auth.dto.UserSocialBindStatusDTO;
import org.xhy.community.application.auth.service.GithubAuthAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.interfaces.oauth.request.GithubCallbackRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.UserContext;

@RestController
@RequestMapping("/api/user/oauth/github")
public class UserOAuthController {

    private final GithubAuthAppService githubAuthAppService;

    public UserOAuthController(GithubAuthAppService githubAuthAppService) {
        this.githubAuthAppService = githubAuthAppService;
    }

    @GetMapping("/status")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "OAUTH_GITHUB_STATUS", name = "GitHub绑定状态")})
    public ApiResponse<UserSocialBindStatusDTO> status() {
        String userId = UserContext.getCurrentUserId();
        return ApiResponse.success(githubAuthAppService.getGithubBindStatus(userId));
    }

    @PostMapping("/bind")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "OAUTH_GITHUB_BIND", name = "GitHub绑定")})
    @ActivityLog(ActivityType.OAUTH_BIND)
    public ApiResponse<UserSocialBindStatusDTO> bind(@Valid @RequestBody GithubCallbackRequest request) {
        String userId = UserContext.getCurrentUserId();
        return ApiResponse.success(githubAuthAppService.bindGithub(userId, request.getCode(), request.getState()));
    }

    @PostMapping("/unbind")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "OAUTH_GITHUB_UNBIND", name = "GitHub解绑")})
    @ActivityLog(ActivityType.OAUTH_UNBIND)
    public ApiResponse<Void> unbind() {
        String userId = UserContext.getCurrentUserId();
        githubAuthAppService.unbindGithub(userId);
        return ApiResponse.success("解绑成功");
    }
}
