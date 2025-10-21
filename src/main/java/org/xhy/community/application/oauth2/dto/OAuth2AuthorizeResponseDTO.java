package org.xhy.community.application.oauth2.dto;

/**
 * OAuth2 授权响应 DTO
 * 授权成功后返回给第三方应用的数据
 */
public class OAuth2AuthorizeResponseDTO {

    /** 授权码 */
    private String code;

    /** State 参数（CSRF 防护） */
    private String state;

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
