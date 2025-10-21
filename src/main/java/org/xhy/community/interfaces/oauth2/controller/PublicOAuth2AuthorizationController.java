package org.xhy.community.interfaces.oauth2.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.oauth2.dto.OAuth2TokenDTO;
import org.xhy.community.application.oauth2.service.OAuth2AuthorizationAppService;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.service.OAuth2ClientDomainService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OAuth2ErrorCode;
import org.xhy.community.interfaces.oauth2.request.OAuth2AuthorizeRequest;
import org.xhy.community.interfaces.oauth2.request.OAuth2TokenRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 授权服务器公开接口
 * 提供标准的 OAuth2 授权端点（前后端分离架构）
 * @module OAuth2授权服务器
 */
@RestController
@RequestMapping("/api/public/oauth2")
public class PublicOAuth2AuthorizationController {

    private final OAuth2AuthorizationAppService authorizationAppService;
    private final OAuth2ClientDomainService clientDomainService;
    private final org.xhy.community.infrastructure.config.JwtUtil jwtUtil;

    /** 前端授权页面URL（从配置文件读取） */
    @Value("${oauth2.frontend.authorize-url:http://localhost:5173/oauth2/authorize}")
    private String frontendAuthorizeUrl;

    public PublicOAuth2AuthorizationController(
            OAuth2AuthorizationAppService authorizationAppService,
            OAuth2ClientDomainService clientDomainService,
            org.xhy.community.infrastructure.config.JwtUtil jwtUtil) {
        this.authorizationAppService = authorizationAppService;
        this.clientDomainService = clientDomainService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 授权端点 - GET 请求
     * 第三方应用跳转到此端点请求授权
     *
     * 流程：
     * 1. 验证客户端和参数
     * 2. 如果用户未登录 → 重定向到前端授权页面（前端负责登录和授权同意）
     * 3. 如果用户已登录但未授权 → 重定向到前端授权页面
     * 4. 如果用户已登录且已授权 → 直接生成授权码并重定向回第三方应用
     *
     * @param request 授权请求参数
     * @param response HTTP响应
     */
    @GetMapping("/authorize")
    public void authorize(@Valid @ModelAttribute OAuth2AuthorizeRequest request,
                         HttpServletResponse response) throws IOException {
        // 验证 response_type 必须是 "code"
        if (!"code".equals(request.getResponseType())) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_GRANT_TYPE, "仅支持 response_type=code");
        }

        // 验证客户端
        OAuth2ClientEntity client = clientDomainService.getClientByClientId(request.getClientId());

        // 验证重定向URI
        if (!client.isValidRedirectUri(request.getRedirectUri())) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_REDIRECT_URI);
        }

        // 获取当前登录用户
        String userId;
        try {
            userId = UserContext.getCurrentUserId();
        } catch (Exception e) {
            // 用户未登录，重定向到前端授权页面
            // 前端授权页面会负责用户登录和授权同意
            redirectToFrontendAuthorizePage(request, response);
            return;
        }

        // 解析 scope
        List<String> scopes = request.getScope() != null
                ? Arrays.asList(request.getScope().split(" "))
                : List.of();

        // 检查用户是否已授权（如果客户端要求授权同意）
        if (client.getRequireAuthorizationConsent()) {
            List<String> consentedScopes = authorizationAppService.getConsentedScopes(request.getClientId(), userId);
            if (consentedScopes == null || !consentedScopes.containsAll(scopes)) {
                // 需要用户确认授权，重定向到前端授权页面
                redirectToFrontendAuthorizePage(request, response);
                return;
            }
        }

        // 用户已授权或不需要授权同意，直接生成授权码
        String authorizationCode = authorizationAppService.generateAuthorizationCode(
                request.getClientId(),
                userId,
                scopes,
                request.getRedirectUri(),
                request.getState()
        );

        // 重定向回第三方应用
        String redirectUrl = buildRedirectUrl(request.getRedirectUri(), authorizationCode, request.getState());
        response.sendRedirect(redirectUrl);
    }

    /**
     * 生成授权码 - POST 请求
     * 前端授权页面在用户同意授权后调用此接口生成授权码
     *
     * @param request 授权请求参数
     * @param approved 是否同意授权
     * @param httpRequest HTTP请求对象（用于提取用户认证信息）
     * @return 授权码（前端需要用此授权码重定向回第三方应用）
     */
    @PostMapping("/authorize")
    public ApiResponse<String> generateAuthorizationCode(
            @Valid @RequestBody OAuth2AuthorizeRequest request,
            @RequestParam(value = "approved", defaultValue = "true") boolean approved,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        if (!approved) {
            // 用户拒绝授权
            throw new BusinessException(OAuth2ErrorCode.AUTHORIZATION_DENIED);
        }

        // 从请求中手动提取用户ID（因为/api/public/**路径不经过UserContextInterceptor）
        String userId = extractUserIdFromRequest(httpRequest);
        if (userId == null) {
            throw new BusinessException(OAuth2ErrorCode.UNAUTHORIZED, "用户未登录或Token无效");
        }

        // 解析 scope
        List<String> scopes = request.getScope() != null
                ? Arrays.asList(request.getScope().split(" "))
                : List.of();

        // 保存用户授权同意
        authorizationAppService.saveConsent(request.getClientId(), userId, scopes);

        // 生成授权码
        String authorizationCode = authorizationAppService.generateAuthorizationCode(
                request.getClientId(),
                userId,
                scopes,
                request.getRedirectUri(),
                request.getState()
        );

        return ApiResponse.success("授权成功", authorizationCode);
    }

    /**
     * 令牌端点
     * 第三方应用使用授权码、刷新令牌或客户端凭证换取 Access Token
     *
     * @param request Token 请求参数
     * @return Token 响应
     */
    @PostMapping("/token")
    public ApiResponse<OAuth2TokenDTO> token(@Valid @RequestBody OAuth2TokenRequest request) {
        OAuth2TokenDTO token = authorizationAppService.getToken(request);
        return ApiResponse.success(token);
    }

    /**
     * 获取客户端信息（公开接口）
     * 前端授权页面用于展示客户端详情
     *
     * @param clientId 客户端ID
     * @return 客户端信息（不包含敏感信息如密钥）
     */
    @GetMapping("/clients/{clientId}")
    public ApiResponse<Map<String, Object>> getClientInfo(@PathVariable String clientId) {
        OAuth2ClientEntity client = clientDomainService.getClientByClientId(clientId);

        // 只返回公开信息，不包含密钥
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("id", client.getId());
        clientInfo.put("clientId", client.getClientId());
        clientInfo.put("clientName", client.getClientName());
        clientInfo.put("redirectUris", client.getRedirectUris());
        clientInfo.put("scopes", client.getScopes());
        clientInfo.put("requireAuthorizationConsent", client.getRequireAuthorizationConsent());
        clientInfo.put("createTime", client.getCreateTime());

        return ApiResponse.success(clientInfo);
    }

    /**
     * 构建重定向URL
     *
     * @param redirectUri 重定向URI
     * @param code 授权码
     * @param state State 参数
     * @return 完整的重定向URL
     */
    private String buildRedirectUrl(String redirectUri, String code, String state) {
        StringBuilder url = new StringBuilder(redirectUri);
        url.append(redirectUri.contains("?") ? "&" : "?");
        url.append("code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
        if (state != null && !state.isEmpty()) {
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    /**
     * 重定向到前端授权页面
     * 将所有授权参数传递给前端
     *
     * @param request 授权请求参数
     * @param response HTTP响应
     */
    private void redirectToFrontendAuthorizePage(OAuth2AuthorizeRequest request, HttpServletResponse response) throws IOException {
        StringBuilder url = new StringBuilder(frontendAuthorizeUrl);
        url.append("?client_id=").append(URLEncoder.encode(request.getClientId(), StandardCharsets.UTF_8));
        url.append("&redirect_uri=").append(URLEncoder.encode(request.getRedirectUri(), StandardCharsets.UTF_8));
        url.append("&response_type=").append(URLEncoder.encode(request.getResponseType(), StandardCharsets.UTF_8));

        if (request.getScope() != null && !request.getScope().isEmpty()) {
            url.append("&scope=").append(URLEncoder.encode(request.getScope(), StandardCharsets.UTF_8));
        }
        if (request.getState() != null && !request.getState().isEmpty()) {
            url.append("&state=").append(URLEncoder.encode(request.getState(), StandardCharsets.UTF_8));
        }
        if (request.getCodeChallenge() != null && !request.getCodeChallenge().isEmpty()) {
            url.append("&code_challenge=").append(URLEncoder.encode(request.getCodeChallenge(), StandardCharsets.UTF_8));
        }
        if (request.getCodeChallengeMethod() != null && !request.getCodeChallengeMethod().isEmpty()) {
            url.append("&code_challenge_method=").append(URLEncoder.encode(request.getCodeChallengeMethod(), StandardCharsets.UTF_8));
        }

        response.sendRedirect(url.toString());
    }

    /**
     * 从HTTP请求中提取用户ID
     * 因为 /api/public/** 路径不经过 UserContextInterceptor，需要手动解析JWT Token
     *
     * @param request HTTP请求
     * @return 用户ID，如果Token无效或不存在则返回null
     */
    private String extractUserIdFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    return jwtUtil.getUserIdFromToken(token);
                }
            } catch (Exception e) {
                // Token解析失败，返回null
                return null;
            }
        }
        return null;
    }
}
