package org.xhy.community.interfaces.oauth2.request;

import jakarta.validation.constraints.NotBlank;

/**
 * OAuth2 Token 请求参数
 * 对应 /oauth2/token 端点
 *
 * 使用驼峰命名（JavaScript 友好）
 */
public class OAuth2TokenRequest {

    /** 授权类型 */
    @NotBlank(message = "grantType不能为空")
    private String grantType;

    /** 客户端ID */
    @NotBlank(message = "clientId不能为空")
    private String clientId;

    /** 客户端密钥 */
    @NotBlank(message = "clientSecret不能为空")
    private String clientSecret;

    // ========== 授权码模式参数 ==========

    /** 授权码 */
    private String code;

    /** 重定向URI（必须与授权时一致） */
    private String redirectUri;

    /** PKCE code_verifier */
    private String codeVerifier;

    // ========== 刷新令牌模式参数 ==========

    /** Refresh Token */
    private String refreshToken;

    // ========== 客户端凭证模式参数 ==========

    /** 权限范围（空格分隔） */
    private String scope;

    // Getters and Setters

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
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
