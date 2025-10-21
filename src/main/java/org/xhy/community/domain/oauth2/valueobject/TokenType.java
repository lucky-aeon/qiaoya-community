package org.xhy.community.domain.oauth2.valueobject;

/**
 * OAuth2 Token 类型枚举
 */
public enum TokenType {

    /**
     * Bearer Token
     * 最常用的 Token 类型，通过 Authorization: Bearer <token> 传递
     */
    BEARER("Bearer", "Bearer Token");

    private final String value;
    private final String description;

    TokenType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
