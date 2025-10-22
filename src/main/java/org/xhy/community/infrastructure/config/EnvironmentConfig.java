package org.xhy.community.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 环境配置类
 * 用于判断当前运行环境并提供环境相关的配置
 */
@Component
public class EnvironmentConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /**
     * 判断是否为本地开发环境
     * @return true 如果是 local 环境
     */
    public boolean isLocal() {
        return "local".equalsIgnoreCase(activeProfile);
    }

    /**
     * 判断是否为生产环境
     * @return true 如果是 prod 环境
     */
    public boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile);
    }

    /**
     * 获取 Cookie 的 domain 配置
     * @return LOCAL 环境返回 null，其他环境返回 .xhyovo.cn
     */
    public String getCookieDomain() {
        return isLocal() ? null : ".xhyovo.cn";
    }

    /**
     * 获取当前激活的环境配置
     * @return 环境名称
     */
    public String getActiveProfile() {
        return activeProfile;
    }
}
