package org.xhy.community.interfaces.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xhy.community.application.subscription.service.UserSubscriptionAppService;
import org.xhy.community.domain.session.service.DeviceSessionDomainService;
import org.xhy.community.domain.config.service.UserSessionConfigService;
import org.xhy.community.domain.config.valueobject.UserSessionConfig;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.util.ClientIpUtil;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserContextInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserSubscriptionAppService userSubscriptionAppService;
    private final DeviceSessionDomainService deviceSessionDomainService;
    private final UserSessionConfigService userSessionConfigService;

    public UserContextInterceptor(JwtUtil jwtUtil,
                                  UserSubscriptionAppService userSubscriptionAppService,
                                  DeviceSessionDomainService deviceSessionDomainService,
                                  UserSessionConfigService userSessionConfigService) {
        this.jwtUtil = jwtUtil;
        this.userSubscriptionAppService = userSubscriptionAppService;
        this.deviceSessionDomainService = deviceSessionDomainService;
        this.userSessionConfigService = userSessionConfigService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = extractUserIdFromRequest(request);

        if (StringUtils.hasText(userId)) {
            UserContext.setCurrentUserId(userId);

            // 基于 IP 的活跃会话校验
            String ip = ClientIpUtil.getClientIp(request);
            boolean active = deviceSessionDomainService.isIpActive(userId, ip);
            if (!active) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 获取用户会话配置
            UserSessionConfig sessionConfig = userSessionConfigService.getUserSessionConfig();

            // 续活（简单策略：每次请求均续活；如需节流可加 Redis 简易令牌）
            deviceSessionDomainService.touchActiveIp(
                userId,
                ip,
                sessionConfig.getTtl().toMillis(),
                sessionConfig.getHistoryWindow().toMillis(),
                sessionConfig.getBanThreshold(),
                sessionConfig.getBanTtl().toMillis()
            );

            // 兜底：确保用户至少拥有一个默认套餐，由应用服务封装具体逻辑
            try {
                userSubscriptionAppService.ensureDefaultSubscriptionIfMissing(userId);
            } catch (Exception e) {
                // 双保险：即便应用层已处理异常，这里也不影响请求
                log.warn("ensureDefaultSubscriptionIfMissing threw, userId={}, err={}", userId, e.getMessage());
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
