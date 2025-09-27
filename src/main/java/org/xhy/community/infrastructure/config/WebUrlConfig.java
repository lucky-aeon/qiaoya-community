package org.xhy.community.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 站点 URL 配置与解析
 */
@Configuration
@ConfigurationProperties(prefix = "community.web")
public class WebUrlConfig {

    /** 站点基础域名，如 https://qiaoya.com */
    private String baseUrl = "https://qiaoya.com";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = trimTrailingSlash(baseUrl); }

    /**
     * 解析相对路径为绝对 URL；若已是 http(s) 开头则原样返回。
     */
    public String resolve(String path) {
        if (path == null || path.isBlank()) return baseUrl;
        String p = path.trim();
        String lower = p.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return p;
        }
        String normalized = p.startsWith("/") ? p : "/" + p;
        return baseUrl + normalized;
    }

    private String trimTrailingSlash(String url) {
        if (url == null) return null;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}

