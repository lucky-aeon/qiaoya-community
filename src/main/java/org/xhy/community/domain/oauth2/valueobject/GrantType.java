package org.xhy.community.domain.oauth2.valueobject;

/**
 * OAuth2 授权类型枚举
 */
public enum GrantType {

    /**
     * 授权码模式
     * 最常用的授权方式，适用于有后端的Web应用
     */
    AUTHORIZATION_CODE("authorization_code", "授权码模式"),

    /**
     * 刷新令牌模式
     * 用于通过 Refresh Token 获取新的 Access Token
     */
    REFRESH_TOKEN("refresh_token", "刷新令牌模式"),

    /**
     * 客户端凭证模式
     * 用于服务端对服务端的授权，无需用户参与
     */
    CLIENT_CREDENTIALS("client_credentials", "客户端凭证模式");

    private final String value;
    private final String description;

    GrantType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据值获取枚举
     */
    public static GrantType fromValue(String value) {
        for (GrantType type : GrantType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown GrantType value: " + value);
    }
}
