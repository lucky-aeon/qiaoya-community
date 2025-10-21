package org.xhy.community.interfaces.oauth2.request;

import jakarta.validation.constraints.NotBlank;

/**
 * OAuth2 授权请求参数
 * 对应 /oauth2/authorize 端点
 *
 * 使用驼峰命名（JavaScript 友好）
 */
public class OAuth2AuthorizeRequest {

    /** 客户端ID */
    @NotBlank(message = "clientId不能为空")
    private String clientId;

    /** 重定向URI */
    @NotBlank(message = "redirectUri不能为空")
    private String redirectUri;

    /** 响应类型（固定为 "code"） */
    @NotBlank(message = "responseType不能为空")
    private String responseType;

    /** 权限范围（空格分隔） */
    private String scope;

    /** State 参数（CSRF 防护） */
    private String state;

    /** PKCE code_challenge */
    private String codeChallenge;

    /** PKCE code_challenge_method */
    private String codeChallengeMethod;

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }
}
