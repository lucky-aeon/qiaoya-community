package org.xhy.community.domain.oauth2.valueobject;

/**
 * OAuth2 权限范围枚举
 * 定义客户端可以请求的权限范围
 */
public enum OAuth2Scope {

    /**
     * OpenID Connect 基础权限
     * 表示这是一个 OIDC 请求
     */
    OPENID("openid", "OpenID Connect"),

    /**
     * 用户基本信息
     * 包括用户名、昵称等基本资料
     */
    PROFILE("profile", "用户基本信息"),

    /**
     * 用户邮箱
     * 包括邮箱地址和验证状态
     */
    EMAIL("email", "用户邮箱"),

    /**
     * 读取用户数据
     * 允许读取用户的公开数据
     */
    READ("read", "读取用户数据"),

    /**
     * 写入用户数据
     * 允许修改用户数据
     */
    WRITE("write", "写入用户数据");

    private final String value;
    private final String description;

    OAuth2Scope(String value, String description) {
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
    public static OAuth2Scope fromValue(String value) {
        for (OAuth2Scope scope : OAuth2Scope.values()) {
            if (scope.value.equals(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown OAuth2Scope value: " + value);
    }

    /**
     * 检查值是否有效
     */
    public static boolean isValid(String value) {
        try {
            fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
