package org.xhy.community.domain.oauth2.valueobject;

/**
 * OAuth2 客户端状态枚举
 */
public enum OAuth2ClientStatus {

    /**
     * 激活状态 - 客户端正常可用
     */
    ACTIVE("激活", "客户端正常可用"),

    /**
     * 暂停状态 - 客户端被暂停使用
     * 可由管理员手动恢复
     */
    SUSPENDED("暂停", "客户端被暂停使用"),

    /**
     * 撤销状态 - 客户端被永久撤销
     * 不可恢复，需要重新注册
     */
    REVOKED("撤销", "客户端被永久撤销");

    private final String label;
    private final String description;

    OAuth2ClientStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
