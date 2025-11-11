package org.xhy.community.application.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OAuth2 Token 响应 DTO
 * 输出字段遵循 OAuth2 标准下划线命名
 */
public class OAuth2TokenDTO {

    /** Access Token */
    @JsonProperty("access_token")
    private String accessToken;

    /** Token 类型（通常是 "Bearer"） */
    @JsonProperty("token_type")
    private String tokenType;

    /** Access Token 有效期（秒） */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /** Refresh Token（可选） */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /** 权限范围（空格分隔） */
    @JsonProperty("scope")
    private String scope;

    // Getters and Setters

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
