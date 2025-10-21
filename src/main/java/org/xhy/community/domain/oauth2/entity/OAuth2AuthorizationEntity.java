package org.xhy.community.domain.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * OAuth2 授权记录实体
 * 存储授权码、Access Token、Refresh Token 等信息
 */
@TableName(value = "oauth2_authorizations", autoResultMap = true)
public class OAuth2AuthorizationEntity extends BaseEntity {

    /** 客户端ID（对应 oauth2_clients.client_id） */
    private String clientId;

    /** 用户标识（用户ID或用户名） */
    private String principalName;

    /** 授权类型（authorization_code、refresh_token、client_credentials） */
    private String authorizationGrantType;

    // ========== 授权码相关 ==========

    /** 授权码值 */
    private String authorizationCodeValue;

    /** 授权码签发时间 */
    private LocalDateTime authorizationCodeIssuedAt;

    /** 授权码过期时间 */
    private LocalDateTime authorizationCodeExpiresAt;

    /** 授权码元数据（JSON） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String authorizationCodeMetadata;

    // ========== Access Token 相关 ==========

    /** Access Token 值 */
    private String accessTokenValue;

    /** Access Token 签发时间 */
    private LocalDateTime accessTokenIssuedAt;

    /** Access Token 过期时间 */
    private LocalDateTime accessTokenExpiresAt;

    /** Access Token 元数据（JSON） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String accessTokenMetadata;

    /** Access Token 类型（Bearer） */
    private String accessTokenType;

    /** Access Token 权限范围（JSON 数组） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String accessTokenScopes;

    // ========== Refresh Token 相关 ==========

    /** Refresh Token 值 */
    private String refreshTokenValue;

    /** Refresh Token 签发时间 */
    private LocalDateTime refreshTokenIssuedAt;

    /** Refresh Token 过期时间 */
    private LocalDateTime refreshTokenExpiresAt;

    /** Refresh Token 元数据（JSON） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String refreshTokenMetadata;

    // ========== OIDC ID Token 相关 ==========

    /** OIDC ID Token 值 */
    private String oidcIdTokenValue;

    /** OIDC ID Token 签发时间 */
    private LocalDateTime oidcIdTokenIssuedAt;

    /** OIDC ID Token 过期时间 */
    private LocalDateTime oidcIdTokenExpiresAt;

    /** OIDC ID Token 元数据（JSON） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String oidcIdTokenMetadata;

    // ========== 其他 ==========

    /** 状态参数（CSRF 防护） */
    private String state;

    /** 扩展属性（JSON） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String attributes;

    // 构造函数
    public OAuth2AuthorizationEntity() {
    }

    // 业务方法

    /**
     * 检查授权码是否有效
     */
    public boolean isAuthorizationCodeValid() {
        return authorizationCodeValue != null &&
               authorizationCodeExpiresAt != null &&
               authorizationCodeExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 检查 Access Token 是否有效
     */
    public boolean isAccessTokenValid() {
        return accessTokenValue != null &&
               accessTokenExpiresAt != null &&
               accessTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 检查 Refresh Token 是否有效
     */
    public boolean isRefreshTokenValid() {
        return refreshTokenValue != null &&
               refreshTokenExpiresAt != null &&
               refreshTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 使授权码失效（用过后立即失效）
     */
    public void invalidateAuthorizationCode() {
        this.authorizationCodeValue = null;
        this.authorizationCodeExpiresAt = LocalDateTime.now().minusSeconds(1);
    }

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAuthorizationGrantType() {
        return authorizationGrantType;
    }

    public void setAuthorizationGrantType(String authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    public String getAuthorizationCodeValue() {
        return authorizationCodeValue;
    }

    public void setAuthorizationCodeValue(String authorizationCodeValue) {
        this.authorizationCodeValue = authorizationCodeValue;
    }

    public LocalDateTime getAuthorizationCodeIssuedAt() {
        return authorizationCodeIssuedAt;
    }

    public void setAuthorizationCodeIssuedAt(LocalDateTime authorizationCodeIssuedAt) {
        this.authorizationCodeIssuedAt = authorizationCodeIssuedAt;
    }

    public LocalDateTime getAuthorizationCodeExpiresAt() {
        return authorizationCodeExpiresAt;
    }

    public void setAuthorizationCodeExpiresAt(LocalDateTime authorizationCodeExpiresAt) {
        this.authorizationCodeExpiresAt = authorizationCodeExpiresAt;
    }

    public String getAuthorizationCodeMetadata() {
        return authorizationCodeMetadata;
    }

    public void setAuthorizationCodeMetadata(String authorizationCodeMetadata) {
        this.authorizationCodeMetadata = authorizationCodeMetadata;
    }

    public String getAccessTokenValue() {
        return accessTokenValue;
    }

    public void setAccessTokenValue(String accessTokenValue) {
        this.accessTokenValue = accessTokenValue;
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

    public String getAccessTokenMetadata() {
        return accessTokenMetadata;
    }

    public void setAccessTokenMetadata(String accessTokenMetadata) {
        this.accessTokenMetadata = accessTokenMetadata;
    }

    public String getAccessTokenType() {
        return accessTokenType;
    }

    public void setAccessTokenType(String accessTokenType) {
        this.accessTokenType = accessTokenType;
    }

    public String getAccessTokenScopes() {
        return accessTokenScopes;
    }

    public void setAccessTokenScopes(String accessTokenScopes) {
        this.accessTokenScopes = accessTokenScopes;
    }

    public String getRefreshTokenValue() {
        return refreshTokenValue;
    }

    public void setRefreshTokenValue(String refreshTokenValue) {
        this.refreshTokenValue = refreshTokenValue;
    }

    public LocalDateTime getRefreshTokenIssuedAt() {
        return refreshTokenIssuedAt;
    }

    public void setRefreshTokenIssuedAt(LocalDateTime refreshTokenIssuedAt) {
        this.refreshTokenIssuedAt = refreshTokenIssuedAt;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public String getRefreshTokenMetadata() {
        return refreshTokenMetadata;
    }

    public void setRefreshTokenMetadata(String refreshTokenMetadata) {
        this.refreshTokenMetadata = refreshTokenMetadata;
    }

    public String getOidcIdTokenValue() {
        return oidcIdTokenValue;
    }

    public void setOidcIdTokenValue(String oidcIdTokenValue) {
        this.oidcIdTokenValue = oidcIdTokenValue;
    }

    public LocalDateTime getOidcIdTokenIssuedAt() {
        return oidcIdTokenIssuedAt;
    }

    public void setOidcIdTokenIssuedAt(LocalDateTime oidcIdTokenIssuedAt) {
        this.oidcIdTokenIssuedAt = oidcIdTokenIssuedAt;
    }

    public LocalDateTime getOidcIdTokenExpiresAt() {
        return oidcIdTokenExpiresAt;
    }

    public void setOidcIdTokenExpiresAt(LocalDateTime oidcIdTokenExpiresAt) {
        this.oidcIdTokenExpiresAt = oidcIdTokenExpiresAt;
    }

    public String getOidcIdTokenMetadata() {
        return oidcIdTokenMetadata;
    }

    public void setOidcIdTokenMetadata(String oidcIdTokenMetadata) {
        this.oidcIdTokenMetadata = oidcIdTokenMetadata;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
}
