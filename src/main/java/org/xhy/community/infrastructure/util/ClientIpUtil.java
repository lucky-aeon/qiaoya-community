package org.xhy.community.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端IP工具类
 * 用于获取客户端真实IP地址，考虑代理和负载均衡场景
 */
public class ClientIpUtil {

    /**
     * 获取客户端真实IP地址
     * 优先级：X-Forwarded-For > X-Real-IP > RemoteAddr
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        // 尝试从 X-Forwarded-For 头获取（代理链中的第一个IP）
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            String[] parts = xff.split(",");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }

        // 尝试从 X-Real-IP 头获取（Nginx等反向代理设置）
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }

        // 兜底使用直接连接的IP
        return request.getRemoteAddr();
    }
}