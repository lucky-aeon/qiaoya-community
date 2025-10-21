package org.xhy.community.domain.oauth2.valueobject;

/**
 * OAuth2 客户端认证方式枚举
 */
public enum ClientAuthenticationMethod {

    /**
     * Basic 认证
     * 客户端ID和密钥通过 HTTP Basic Authentication 传递
     */
    CLIENT_SECRET_BASIC("client_secret_basic", "Basic认证"),

    /**
     * POST 参数认证
     * 客户端ID和密钥通过 POST 请求体参数传递
     */
    CLIENT_SECRET_POST("client_secret_post", "POST参数认证"),

    /**
     * 无需认证
     * 适用于公开客户端（如纯前端应用），不需要客户端密钥
     */
    NONE("none", "无需认证（公开客户端）");

    private final String value;
    private final String description;

    ClientAuthenticationMethod(String value, String description) {
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
    public static ClientAuthenticationMethod fromValue(String value) {
        for (ClientAuthenticationMethod method : ClientAuthenticationMethod.values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown ClientAuthenticationMethod value: " + value);
    }
}
