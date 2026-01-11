package org.xhy.community.domain.codex.valueobject;

/**
 * Codex 集成配置（存于 Redis）
 */
public class CodexConfig {
    private String baseUrl;       // e.g. https://codex.packycode.com
    private String apiKey;        // 发放给前端使用
    private String authorization; // 后端调用上游接口使用（Bearer token）
    private String cookieToken;   // 可选：上游要求的 Cookie token
    private Boolean enabled;      // 是否启用
    private String expiresAt;     // 可选：到期时间（ISO-8601）
    private String lastUpdatedAt; // 最近更新时间（ISO-8601）
    private String usageDocUrl;  // 使用文档地址（供前台展示）

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getAuthorization() { return authorization; }
    public void setAuthorization(String authorization) { this.authorization = authorization; }
    public String getCookieToken() { return cookieToken; }
    public void setCookieToken(String cookieToken) { this.cookieToken = cookieToken; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(String lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public String getUsageDocUrl() { return usageDocUrl; }
    public void setUsageDocUrl(String usageDocUrl) { this.usageDocUrl = usageDocUrl; }
}
