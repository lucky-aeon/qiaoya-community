package org.xhy.community.interfaces.oauth2.request;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 创建OAuth2客户端请求对象
 */
public class CreateOAuth2ClientRequest {

    /** 客户端ID（对外暴露，必须唯一） */
    @NotBlank(message = "客户端ID不能为空")
    @Size(min = 3, max = 100, message = "客户端ID长度必须在3-100之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "客户端ID只能包含字母、数字、下划线和连字符")
    private String clientId;

    /** 客户端名称 */
    @NotBlank(message = "客户端名称不能为空")
    @Size(max = 200, message = "客户端名称长度不能超过200")
    private String clientName;

    /** 重定向URI列表 */
    @NotEmpty(message = "重定向URI列表不能为空")
    private List<String> redirectUris;

    /** 授权类型列表 */
    @NotEmpty(message = "授权类型列表不能为空")
    private List<String> grantTypes;

    /** 允许的Scope列表 */
    @NotEmpty(message = "Scope列表不能为空")
    private List<String> scopes;

    /** 客户端认证方式列表 */
    @NotEmpty(message = "客户端认证方式列表不能为空")
    private List<String> clientAuthenticationMethods;

    /** Access Token有效期（秒） */
    @NotNull(message = "Access Token有效期不能为空")
    @Min(value = 60, message = "Access Token有效期至少60秒")
    @Max(value = 86400, message = "Access Token有效期最多24小时")
    private Integer accessTokenValiditySeconds;

    /** Refresh Token有效期（秒） */
    @NotNull(message = "Refresh Token有效期不能为空")
    @Min(value = 3600, message = "Refresh Token有效期至少1小时")
    @Max(value = 31536000, message = "Refresh Token有效期最多1年")
    private Integer refreshTokenValiditySeconds;

    /** 是否强制要求PKCE */
    @NotNull(message = "是否强制要求PKCE不能为空")
    private Boolean requireProofKey;

    /** 是否需要用户授权同意 */
    @NotNull(message = "是否需要用户授权同意不能为空")
    private Boolean requireAuthorizationConsent;

    // Getters and Setters

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
}
