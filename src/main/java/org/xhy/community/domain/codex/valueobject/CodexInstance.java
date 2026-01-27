package org.xhy.community.domain.codex.valueobject;

/**
 * Codex 单实例配置（持久化为 SystemConfig 中 JSON 的元素）
 */
public class CodexInstance {
    private String id;           // 本实例唯一ID（UUID）
    private String name;         // 实例显示名称/别名
    private String baseUrl;      // e.g. https://codex.packycode.com
    private String apiKey;       // 供前端展示使用
    private String authorization; // 后端调用上游接口使用（Bearer token）
    private String cookieToken;  // 可选：上游要求的 Cookie token
    private Boolean enabled;     // 是否启用
    private String expiresAt;    // 可选：到期时间（ISO-8601）
    private String lastUpdatedAt;// 最近更新时间（ISO-8601）
    private String usageDocUrl;  // 使用文档地址（供前台展示）

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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

