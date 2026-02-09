package org.xhy.community.infrastructure.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.xhy.community.infrastructure.config.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * WebSocket 握手阶段的 JWT 校验拦截器
 *
 * 作用：
 * - 从 URL query `token` 或 `Authorization: Bearer xxx` 中解析 JWT
 * - 校验 token 有效性，解析 userId，并存入 WebSocket session attributes
 * - 拒绝未通过校验的握手（返回 401）
 *
 * 注意：
 * - 仅在握手阶段做轻量校验；业务房间权限校验在 ChatWebSocketHandler 内通过 App 层完成
 */
@Component
public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    public static final String ATTR_USER_ID = "userId";

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (!(request instanceof ServletServerHttpRequest servletReq) || !(response instanceof ServletServerHttpResponse servletResp)) {
            return super.beforeHandshake(request, response, wsHandler, attributes);
        }
        HttpServletRequest httpReq = servletReq.getServletRequest();
        HttpServletResponse httpResp = servletResp.getServletResponse();
        try {
            // 允许两种携带方式：query `?token=` 或 HTTP Header `Authorization: Bearer <token>`
            String token = extractToken(httpReq);
            if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
                log.warn("[WS] 握手失败：无效token");
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            String userId = jwtUtil.getUserIdFromToken(token);
            if (!StringUtils.hasText(userId)) {
                log.warn("[WS] 握手失败：无法解析userId");
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            // 将 userId 放入 session attributes，供后续消息处理与在线态判定使用
            attributes.put(ATTR_USER_ID, userId);
            return super.beforeHandshake(request, response, wsHandler, attributes);
        } catch (Exception e) {
            log.warn("[WS] 握手异常", e);
            httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 可根据需要扩展
        super.afterHandshake(request, response, wsHandler, exception);
    }

    /**
     * 提取 token：优先取 query `token`，否则回退到 Authorization 头
     */
    private String extractToken(HttpServletRequest request) {
        // 1) query param token
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) return token;
        // 2) Authorization header
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}
