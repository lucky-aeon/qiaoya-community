package org.xhy.community.domain.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;
import org.xhy.community.infrastructure.converter.OAuth2ClientStatusConverter;

import java.util.List;

/**
 * OAuth2 客户端实体
 * 存储第三方应用的客户端信息
 */
@TableName(value = "oauth2_clients", autoResultMap = true)
public class OAuth2ClientEntity extends BaseEntity {

    /** 客户端ID（对外暴露，用于标识客户端） */
    private String clientId;

    /** 客户端密钥（加密存储） */
    private String clientSecretEnc;

    /** 客户端名称（应用名称） */
    private String clientName;

    /** 重定向URI列表（JSON存储） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> redirectUris;

    /** 授权类型列表（JSON存储，如 authorization_code, refresh_token） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> grantTypes;

    /** 允许的Scope列表（JSON存储，如 openid, profile, email） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scopes;

    /** 客户端认证方式（JSON存储，如 client_secret_basic, client_secret_post） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> clientAuthenticationMethods;

    /** Access Token有效期（秒，默认1小时） */
    private Integer accessTokenValiditySeconds;

    /** Refresh Token有效期（秒，默认30天） */
    private Integer refreshTokenValiditySeconds;

    /** 是否强制要求PKCE（防止授权码拦截攻击） */
    private Boolean requireProofKey;

    /** 是否需要用户授权同意 */
    private Boolean requireAuthorizationConsent;

    /** 客户端状态 */
    @TableField(typeHandler = OAuth2ClientStatusConverter.class)
    private OAuth2ClientStatus status;

    /** 创建人用户ID */
    private String createdBy;

    // 构造函数

    public OAuth2ClientEntity() {
        this.status = OAuth2ClientStatus.ACTIVE;
        this.accessTokenValiditySeconds = 3600; // 默认1小时
        this.refreshTokenValiditySeconds = 2592000; // 默认30天
        this.requireProofKey = false;
        this.requireAuthorizationConsent = true;
    }

    // 业务方法

    /**
     * 激活客户端
     */
    public void activate() {
        this.status = OAuth2ClientStatus.ACTIVE;
    }

    /**
     * 暂停客户端
     */
    public void suspend() {
        this.status = OAuth2ClientStatus.SUSPENDED;
    }

    /**
     * 撤销客户端
     */
    public void revoke() {
        this.status = OAuth2ClientStatus.REVOKED;
    }

    /**
     * 检查客户端是否可用
     */
    public boolean isActive() {
        return OAuth2ClientStatus.ACTIVE.equals(this.status);
    }

    /**
     * 验证重定向URI是否合法
     */
    public boolean isValidRedirectUri(String redirectUri) {
        return redirectUris != null && redirectUris.contains(redirectUri);
    }

    /**
     * 验证授权类型是否支持
     */
    public boolean isGrantTypeSupported(String grantType) {
        return grantTypes != null && grantTypes.contains(grantType);
    }

    /**
     * 验证Scope是否允许
     */
    public boolean isScopeAllowed(String scope) {
        return scopes != null && scopes.contains(scope);
    }

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecretEnc() {
        return clientSecretEnc;
    }

    public void setClientSecretEnc(String clientSecretEnc) {
        this.clientSecretEnc = clientSecretEnc;
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

    public OAuth2ClientStatus getStatus() {
        return status;
    }

    public void setStatus(OAuth2ClientStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
