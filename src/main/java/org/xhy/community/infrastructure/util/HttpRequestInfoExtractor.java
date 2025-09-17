package org.xhy.community.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xhy.community.infrastructure.context.UserActivityContext;

/**
 * HTTP请求信息提取工具类
 * 用于提取客户端的IP、User-Agent、浏览器、设备等信息
 */
public class HttpRequestInfoExtractor {
    
    /**
     * 从当前请求上下文中提取用户活动上下文信息
     * 
     * @return 用户活动上下文，如果无法获取则返回默认值
     */
    public static UserActivityContext extractUserActivityContext() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return createDefaultContext();
            }
            
            HttpServletRequest request = attributes.getRequest();
            String ip = getClientIp(request);
            String userAgent = getUserAgent(request);
            String browser = extractBrowserInfo(request);
            String equipment = extractEquipmentInfo(request);
            
            return new UserActivityContext(ip, userAgent, browser, equipment);
        } catch (Exception e) {
            // 如果获取失败，返回默认上下文，确保业务不受影响
            return createDefaultContext();
        }
    }
    
    /**
     * 创建默认的用户活动上下文
     */
    private static UserActivityContext createDefaultContext() {
        return new UserActivityContext("unknown", "unknown", "unknown", "unknown");
    }
    
    /**
     * 获取客户端真实IP地址
     * 优先从代理头中获取，如果没有则从RemoteAddr获取
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多个代理的情况，第一个IP为客户端真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }
        
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 获取User-Agent信息
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : "unknown";
    }
    
    /**
     * 从User-Agent中提取浏览器信息
     */
    public static String extractBrowserInfo(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        if ("unknown".equals(userAgent)) {
            return "unknown";
        }
        
        String lowerUserAgent = userAgent.toLowerCase();
        
        if (lowerUserAgent.contains("edg/")) {
            return "Microsoft Edge";
        } else if (lowerUserAgent.contains("chrome")) {
            return "Google Chrome";
        } else if (lowerUserAgent.contains("firefox")) {
            return "Mozilla Firefox";
        } else if (lowerUserAgent.contains("safari") && !lowerUserAgent.contains("chrome")) {
            return "Safari";
        } else if (lowerUserAgent.contains("opera") || lowerUserAgent.contains("opr/")) {
            return "Opera";
        } else if (lowerUserAgent.contains("trident") || lowerUserAgent.contains("msie")) {
            return "Internet Explorer";
        } else {
            return "Other";
        }
    }
    
    /**
     * 从User-Agent中提取设备信息
     */
    public static String extractEquipmentInfo(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        if ("unknown".equals(userAgent)) {
            return "unknown";
        }
        
        String lowerUserAgent = userAgent.toLowerCase();
        
        if (lowerUserAgent.contains("mobile") || lowerUserAgent.contains("android") || 
            lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad")) {
            if (lowerUserAgent.contains("android")) {
                return "Android";
            } else if (lowerUserAgent.contains("iphone")) {
                return "iPhone";
            } else if (lowerUserAgent.contains("ipad")) {
                return "iPad";
            } else {
                return "Mobile";
            }
        } else if (lowerUserAgent.contains("windows")) {
            return "Windows";
        } else if (lowerUserAgent.contains("macintosh") || lowerUserAgent.contains("mac os")) {
            return "macOS";
        } else if (lowerUserAgent.contains("linux")) {
            return "Linux";
        } else {
            return "Desktop";
        }
    }
}