package org.xhy.community.interfaces.public_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.ResourceDTO;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.application.session.service.TokenBlacklistAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.interfaces.resource.request.OssCallbackRequest;

import java.net.URI;
import java.util.Map;

/**
 * 公开资源管理控制器
 * 提供与第三方服务集成的公开接口，无需用户认证
 * @module 公开API
 */
@RestController
@RequestMapping("/api/public")
public class PublicResourceController {
    private static final Logger log = LoggerFactory.getLogger(PublicResourceController.class);

    private final ResourceAppService resourceAppService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistAppService tokenBlacklistAppService;
    
    public PublicResourceController(ResourceAppService resourceAppService,
                                   JwtUtil jwtUtil,
                                   TokenBlacklistAppService tokenBlacklistAppService) {
        this.resourceAppService = resourceAppService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistAppService = tokenBlacklistAppService;
    }
    
    /**
     * 处理阿里云OSS上传回调
     * 接收阿里云OSS服务器的上传成功回调通知，并保存文件元数据
     * 回调参数需携带 token 并进行鉴权
     * 
     * @param callbackRequest OSS回调请求参数，包含上传文件的元数据信息与鉴权token
     * @param request HTTP请求对象
     * @return 回调处理结果，返回给OSS服务的响应
     */
    @PostMapping("/oss-callback")
    public ResponseEntity<Map<String, Object>> handleOssCallback(
            @Valid OssCallbackRequest callbackRequest,
            HttpServletRequest request
    ) {
        // 读取并校验回调token
        String token = callbackRequest.getToken();
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Status", "UNAUTHORIZED", "message", "missing token"));
        }

        // 黑名单校验
        if (tokenBlacklistAppService.isBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Status", "UNAUTHORIZED", "message", "token blacklisted"));
        }

        // JWT 有效性校验
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Status", "UNAUTHORIZED", "message", "invalid token"));
        }

        // 通过鉴权后再处理回调
        ResourceDTO resource = resourceAppService.handleOssCallback(callbackRequest);
        
        Map<String, Object> response = Map.of("Status", "OK","resource",resource);
        return ResponseEntity.ok(response);
    }

    /**
     * 资源访问（Cookie/Bearer 双通道鉴权）
     * - 优先从 HttpOnly Cookie: RAUTH 读取 token；若不存在则回退到 Authorization Bearer
     * - 校验通过后记录访问信息并重定向至OSS签名URL
     */
    @GetMapping("/resource/{resourceId}/access")
    public ResponseEntity<Void> accessResource(@PathVariable String resourceId,
                                               @CookieValue(name = "RAUTH", required = false) String rauthCookie,
                                               @RequestHeader(value = "Authorization", required = false) String authorization,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        String token = null;
        if (StringUtils.hasText(rauthCookie)) {
            token = rauthCookie;
        } else if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 黑名单校验
        if (tokenBlacklistAppService.isBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // JWT 有效期校验
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 解析用户ID（用于日志记录）
        String userId = jwtUtil.getUserIdFromToken(token);
        try {
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            log.info("[resource-access] user={} resource={} ip={} ua={} referer={}", userId, resourceId, ip, ua, referer);
        } catch (Exception ignore) {}

        // 生成带签名的直链并重定向
        String accessUrl = resourceAppService.getResourceAccessUrl(resourceId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(accessUrl))
                .build();
    }
}
