package org.xhy.community.application.oauth2.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2 客户端 DTO
 * 用于对外输出客户端信息（不包含密钥）
 */
public class OAuth2ClientDTO {

    /** 主键ID */
    private String id;

    /** 客户端ID */
    private String clientId;

    /** 客户端名称 */
    private String clientName;

    /** 重定向URI列表 */
    private List<String> redirectUris;

    /** 授权类型列表 */
    private List<String> grantTypes;

    /** 允许的Scope列表 */
    private List<String> scopes;

    /** 客户端认证方式列表 */
    private List<String> clientAuthenticationMethods;

    /** Access Token有效期（秒） */
    private Integer accessTokenValiditySeconds;

    /** Refresh Token有效期（秒） */
    private Integer refreshTokenValiditySeconds;

    /** 是否强制要求PKCE */
    private Boolean requireProofKey;

    /** 是否需要用户授权同意 */
    private Boolean requireAuthorizationConsent;

    /** 客户端状态 */
    private String status;

    /** 创建人用户ID */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getClientAuthenticationMethods() {
        return clientAuthenticationMethods;
    }

    public void setClientAuthenticationMethods(List<String> clientAuthenticationMethods) {
        this.clientAuthenticationMethods = clientAuthenticationMethods;
    }

    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public Boolean getRequireProofKey() {
        return requireProofKey;
    }

    public void setRequireProofKey(Boolean requireProofKey) {
        this.requireProofKey = requireProofKey;
    }

    public Boolean getRequireAuthorizationConsent() {
        return requireAuthorizationConsent;
    }

    public void setRequireAuthorizationConsent(Boolean requireAuthorizationConsent) {
        this.requireAuthorizationConsent = requireAuthorizationConsent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
