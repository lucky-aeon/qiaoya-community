package org.xhy.community.interfaces.webconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xhy.community.application.subscription.service.UserSubscriptionAppService;
import org.xhy.community.application.session.service.DeviceSessionAppService;
import org.xhy.community.application.session.service.TokenBlacklistAppService;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.util.ClientIpUtil;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserContextInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserSubscriptionAppService userSubscriptionAppService;
    private final DeviceSessionAppService deviceSessionAppService;
    private final TokenBlacklistAppService tokenBlacklistAppService;
    private final UserAppService userAppService;

    public UserContextInterceptor(JwtUtil jwtUtil,
                                  UserSubscriptionAppService userSubscriptionAppService,
                                  DeviceSessionAppService deviceSessionAppService,
                                  TokenBlacklistAppService tokenBlacklistAppService,
                                  UserAppService userAppService) {
        this.jwtUtil = jwtUtil;
        this.userSubscriptionAppService = userSubscriptionAppService;
        this.deviceSessionAppService = deviceSessionAppService;
        this.tokenBlacklistAppService = tokenBlacklistAppService;
        this.userAppService = userAppService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = extractUserIdFromRequest(request);

        if (StringUtils.hasText(userId)) {
            UserContext.setCurrentUserId(userId);

            // 基于 IP 的设备白名单检查（无锁，纯读操作）
            String ip = ClientIpUtil.getClientIp(request);
            boolean ipAllowed = deviceSessionAppService.isIpAllowed(userId, ip);
            if (!ipAllowed) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 用户状态检查：确保用户处于激活状态
            boolean userActive = userAppService.isUserActive(userId);
            if (!userActive) {
                log.warn("访问被拒绝：用户已被禁用，userId={}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 仅进行订阅有效性校验（兜底绑定在注册/登录/获取用户信息时处理）
            try {
                boolean hasActive = userSubscriptionAppService.hasActiveSubscription(userId);
                if (!hasActive) {
                    log.warn("访问被拒绝：当前用户没有有效订阅，userId={}", userId);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            } catch (Exception e) {
                log.error("订阅校验失败，userId={}，错误={}", userId, e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        // 从Authorization头获取JWT token
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            // 首先检查token是否在黑名单中
            if (tokenBlacklistAppService.isBlacklisted(token)) {
                log.warn("Token已被列入黑名单: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                return null;
            }

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
