package org.xhy.community.interfaces.oauth2.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * OAuth2 Token 请求参数
 * 对应 /oauth2/token 端点
 *
 * 说明：对外参数遵循 OAuth2 标准（下划线命名），
 * 使用 @JsonProperty 指定主名为下划线；同时提供下划线 setter 以兼容表单绑定。
 */
public class OAuth2TokenRequest {

    /** 授权类型 */
    @NotBlank(message = "grant_type不能为空")
    @JsonProperty("grant_type")
    @JsonAlias("grantType")
    private String grantType;

    /** 客户端ID */
    @NotBlank(message = "client_id不能为空")
    @JsonProperty("client_id")
    @JsonAlias("clientId")
    private String clientId;

    /** 客户端密钥 */
    @NotBlank(message = "client_secret不能为空")
    @JsonProperty("client_secret")
    @JsonAlias("clientSecret")
    private String clientSecret;

    // ========== 授权码模式参数 ==========

    /** 授权码 */
    @JsonProperty("code")
    private String code;

    /** 重定向URI（必须与授权时一致） */
    @JsonProperty("redirect_uri")
    @JsonAlias("redirectUri")
    private String redirectUri;

    /** PKCE code_verifier */
    @JsonProperty("code_verifier")
    @JsonAlias("codeVerifier")
    private String codeVerifier;

    // ========== 刷新令牌模式参数 ==========

    /** Refresh Token */
    @JsonProperty("refresh_token")
    @JsonAlias("refreshToken")
    private String refreshToken;

    // ========== 客户端凭证模式参数 ==========

    /** 权限范围（空格分隔） */
    @JsonProperty("scope")
    private String scope;

    // Getters and Setters

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    // 兼容下划线命名：grant_type（表单绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setGrant_type(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    // 兼容下划线命名：client_id（表单绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setClient_id(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    // 兼容下划线命名：client_secret（表单绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setClient_secret(String clientSecret) {
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

    // 兼容下划线命名：redirect_uri（表单绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setRedirect_uri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    // 兼容下划线命名：code_verifier（表单绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setCode_verifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 兼容下划线命名：refresh_token（表单绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setRefresh_token(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    // 兼容下划线命名（与 scope 同名，保留别名）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void set_scope(String scope) {
        this.scope = scope;
    }
}
