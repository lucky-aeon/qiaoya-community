package org.xhy.community.interfaces.oauth2.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * OAuth2 授权请求参数
 * 对应 /oauth2/authorize 端点
 *
 * 说明：对外参数遵循 OAuth2 标准（下划线命名），
 * 通过 @JsonAlias 和下划线 setter 方法兼容 client_id、redirect_uri 等参数。
 */
public class OAuth2AuthorizeRequest {

    /** 客户端ID */
    @NotBlank(message = "client_id不能为空")
    @JsonAlias("client_id")
    private String clientId;

    /** 重定向URI */
    @NotBlank(message = "redirect_uri不能为空")
    @JsonAlias("redirect_uri")
    private String redirectUri;

    /** 响应类型（固定为 "code"） */
    @NotBlank(message = "response_type不能为空")
    @JsonAlias("response_type")
    private String responseType;

    /** 权限范围（空格分隔） */
    @JsonAlias("scope")
    private String scope;

    /** State 参数（CSRF 防护） */
    @JsonAlias("state")
    private String state;

    /** PKCE code_challenge */
    @JsonAlias("code_challenge")
    private String codeChallenge;

    /** PKCE code_challenge_method */
    @JsonAlias("code_challenge_method")
    private String codeChallengeMethod;

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    // 兼容下划线命名：client_id（用于 @ModelAttribute 表单/查询参数绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setClient_id(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    // 兼容下划线命名：redirect_uri（用于 @ModelAttribute 表单/查询参数绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setRedirect_uri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    // 兼容下划线命名：response_type（用于 @ModelAttribute 表单/查询参数绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setResponse_type(String responseType) {
        this.responseType = responseType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    // 移除不必要的 set_scope（"scope" 与字段同名，@ModelAttribute 可直接绑定）

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    // 移除不必要的 set_state（"state" 与字段同名，@ModelAttribute 可直接绑定）

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    // 兼容下划线命名：code_challenge（用于 @ModelAttribute 表单/查询参数绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setCode_challenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    // 兼容下划线命名：code_challenge_method（用于 @ModelAttribute 表单/查询参数绑定）
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setCode_challenge_method(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }
}
