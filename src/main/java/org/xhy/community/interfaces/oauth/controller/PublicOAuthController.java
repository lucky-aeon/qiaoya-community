package org.xhy.community.interfaces.oauth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.auth.dto.AuthorizeUrlDTO;
import org.xhy.community.application.auth.dto.AuthDTO;
import org.xhy.community.application.auth.service.GithubAuthAppService;
import org.xhy.community.domain.auth.service.OAuthRateLimitDomainService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.util.ClientIpUtil;

@RestController
@RequestMapping("/api/oauth/github")
public class PublicOAuthController {

    private final GithubAuthAppService githubAuthAppService;
    private final OAuthRateLimitDomainService rateLimitDomainService;

    public PublicOAuthController(GithubAuthAppService githubAuthAppService,
                                 OAuthRateLimitDomainService rateLimitDomainService) {
        this.githubAuthAppService = githubAuthAppService;
        this.rateLimitDomainService = rateLimitDomainService;
    }

    @GetMapping("/url")
    @ActivityLog(ActivityType.OAUTH_AUTHORIZE_URL)
    public ApiResponse<AuthorizeUrlDTO> getAuthorizeUrl(HttpServletRequest request) {
        String ip = ClientIpUtil.getClientIp(request);
        rateLimitDomainService.checkUrlRateLimit(ip);
        return ApiResponse.success(githubAuthAppService.getAuthorizeUrl());
    }

    @GetMapping("/callback")
    @ActivityLog(ActivityType.OAUTH_CALLBACK)
    public ApiResponse<AuthDTO> callback(@RequestParam("code") String code,
                                         @RequestParam("state") String state,
                                         HttpServletRequest request) {
        String ip = ClientIpUtil.getClientIp(request);
        rateLimitDomainService.checkCallbackRateLimit(ip);
        return ApiResponse.success(githubAuthAppService.handleCallback(code, state));
    }
}
