package org.xhy.community.application.oauth2.dto;

import java.time.LocalDateTime;

/**
 * 用户授权信息 DTO
 * 用于展示用户已授权的第三方应用列表
 */
public class UserAuthorizationDTO {

    /** 授权ID */
    private String id;

    /** 客户端ID */
    private String clientId;

    /** 客户端名称 */
    private String clientName;

    /** 客户端Logo URL */
    private String clientLogoUrl;

    /** 客户端描述 */
    private String clientDescription;

    /** 授权的权限范围 */
    private String scopes;

    /** Access Token 签发时间 */
    private LocalDateTime accessTokenIssuedAt;

    /** Access Token 过期时间 */
    private LocalDateTime accessTokenExpiresAt;

    /** Access Token 是否有效 */
    private Boolean accessTokenValid;

    /** Refresh Token 过期时间 */
    private LocalDateTime refreshTokenExpiresAt;

    /** 授权时间（创建时间） */
    private LocalDateTime createTime;

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

    public String getClientLogoUrl() {
        return clientLogoUrl;
    }

    public void setClientLogoUrl(String clientLogoUrl) {
        this.clientLogoUrl = clientLogoUrl;
    }

    public String getClientDescription() {
        return clientDescription;
    }

    public void setClientDescription(String clientDescription) {
        this.clientDescription = clientDescription;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public LocalDateTime getAccessTokenIssuedAt() {
        return accessTokenIssuedAt;
    }

    public void setAccessTokenIssuedAt(LocalDateTime accessTokenIssuedAt) {
        this.accessTokenIssuedAt = accessTokenIssuedAt;
    }

    public LocalDateTime getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public Boolean getAccessTokenValid() {
        return accessTokenValid;
    }

    public void setAccessTokenValid(Boolean accessTokenValid) {
        this.accessTokenValid = accessTokenValid;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
