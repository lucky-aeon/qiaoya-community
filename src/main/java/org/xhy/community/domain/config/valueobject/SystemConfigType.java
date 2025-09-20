package org.xhy.community.domain.config.valueobject;

public enum SystemConfigType {
    DEFAULT_SUBSCRIPTION_PLAN("新用户默认套餐配置"),
    EMAIL_TEMPLATE("邮件模板配置"),
    SYSTEM_MAINTENANCE("系统维护配置");

    private final String description;

    SystemConfigType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}