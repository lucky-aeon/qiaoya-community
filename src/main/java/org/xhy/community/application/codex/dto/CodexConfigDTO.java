package org.xhy.community.application.codex.dto;

public class CodexConfigDTO {
    private String baseUrl;
    private String apiKey;
    private String authorization;
    private String cookieToken;
    private Boolean enabled;
    private String expiresAt;
    private String lastUpdatedAt;
    private String usageDocUrl;

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
