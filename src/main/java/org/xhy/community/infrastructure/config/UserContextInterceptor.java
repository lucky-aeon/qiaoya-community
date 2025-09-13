package org.xhy.community.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {
    

    private final JwtUtil jwtUtil;

    public UserContextInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = extractUserIdFromRequest(request);
        
        if (StringUtils.hasText(userId)) {
            UserContext.setCurrentUserId(userId);
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
            // Token解析失败，记录日志（可选）
            System.err.println("JWT token解析失败: " + e.getMessage());
        }
        return null;
    }
}