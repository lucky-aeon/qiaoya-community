package org.xhy.community.interfaces.cdn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.session.service.TokenBlacklistAppService;
import org.xhy.community.application.permission.service.UserPermissionAppService;
import org.xhy.community.infrastructure.config.JwtUtil;

/**
 * CDN 鉴权控制器
 * 用于阿里云 CDN 远程鉴权
 * @module CDN鉴权
 */
@RestController
@RequestMapping("/api/public/cdn")
public class CdnAuthController {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistAppService tokenBlacklistAppService;
    private final UserPermissionAppService userPermissionAppService;

    public CdnAuthController(JwtUtil jwtUtil,
                            TokenBlacklistAppService tokenBlacklistAppService,
                            UserPermissionAppService userPermissionAppService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistAppService = tokenBlacklistAppService;
        this.userPermissionAppService = userPermissionAppService;
    }

    /**
     * CDN 远程鉴权接口
     * 阿里云 CDN 在资源请求时会调用此接口进行鉴权
     *
     * 鉴权方式（支持两种，按优先级）：
     * 1. Cookie 方式（优先）：读取 Cookie 中的 RAUTH token（DEV/PROD 环境）
     * 2. URL 参数方式（降级）：读取 URL 参数中的 token（LOCAL 环境）
     *
     * 验证流程：
     * 1. 验证 token 是否存在
     * 2. 验证 token 是否在黑名单中
     * 3. 验证 token 是否过期
     * 4. 业务权限校验（如果提供了 resourceId）：
     *    - 资源已绑定课程：用户直购课程 OR 套餐包含课程 → 允许
     *    - 资源未绑定课程：套餐等级1用户 → 拒绝；其他用户 → 允许
     *
     * @param rauthToken 从 Cookie 中读取的 RAUTH token
     * @param urlToken 从 URL 参数中读取的 token
     * @param resourceId resourceId，用来权限校验
     * @return 200 允许访问，403 拒绝访问
     */
    @GetMapping("/auth")
    public ResponseEntity<Void> auth(
            @CookieValue(name = "RAUTH", required = false) String rauthToken,
            @RequestParam(name = "token", required = false) String urlToken,
            @RequestParam(required = false) String resourceId,
            HttpServletRequest request
    ) {
        // 1. 优先使用 Cookie 中的 token，其次使用 URL 参数中的 token
        String token = StringUtils.hasText(rauthToken) ? rauthToken : urlToken;

        // 2. 验证 token 是否存在
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.status(403).build();
        }

        // 3. 验证 token 是否在黑名单
        if (tokenBlacklistAppService.isBlacklisted(token)) {
            return ResponseEntity.status(403).build();
        }

        // 4. 验证 token 是否过期
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(403).build();
        }

        // 5. 业务权限校验：判断用户是否有权限访问该资源
        if (StringUtils.hasText(resourceId)) {
            String userId = jwtUtil.getUserIdFromToken(token);
            boolean allowed = userPermissionAppService.hasDownloadPermissionForResource(userId, resourceId);
            if (!allowed) {
                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok().build();
    }
}
