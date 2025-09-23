package org.xhy.community.interfaces.webconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserAppService userAppService;

    public AdminAuthInterceptor(JwtUtil jwtUtil, UserAppService userAppService) {
        this.jwtUtil = jwtUtil;
        this.userAppService = userAppService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = extractUserIdFromRequest(request);

        if (!StringUtils.hasText(userId)) {
            log.warn("管理员接口访问失败：未提供有效的用户身份信息, path={}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            if (!userAppService.isAdmin(userId)) {
                log.warn("非管理员用户尝试访问管理员接口: userId={}, path={}", userId, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            return true;

        } catch (BusinessException e) {
            if (e.getErrorCode().equals(UserErrorCode.USER_NOT_FOUND)) {
                log.warn("用户不存在，拒绝访问管理员接口: userId={}, path={}", userId, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            throw e;
        }
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        // 从Authorization头获取JWT token
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            return parseUserIdFromToken(token);
        }
        return null;
    }

    private String parseUserIdFromToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            log.warn("JWT token解析失败: {}", e.getMessage());
        }
        return null;
    }
}
