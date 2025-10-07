package org.xhy.community.infrastructure.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 应用运行环境提供器：基于 active profiles 推断 DeploymentEnv。
 * 依赖关系：任何层均可使用（Infrastructure 组件）。
 */
@Component
public class AppEnvironment {
    private final Environment environment;

    public AppEnvironment(Environment environment) {
        this.environment = environment;
    }

    public DeploymentEnv getEnv() {
        // 优先使用 Environment 的 activeProfiles
        String[] actives = environment.getActiveProfiles();
        if (actives != null && actives.length > 0) {
            // 取第一个有效 profile 进行判断（支持兼容只传单一ENVIRONMENT）
            String first = Arrays.stream(actives)
                    .filter(p -> p != null && !p.isBlank())
                    .findFirst()
                    .orElse(null);
            return DeploymentEnv.from(first);
        }
        // 回退读取 spring.profiles.active 属性
        String active = environment.getProperty("spring.profiles.active");
        return DeploymentEnv.from(active);
    }

    public boolean isDev() {
        return getEnv() == DeploymentEnv.DEV;
    }

    public boolean isProd() {
        return getEnv() == DeploymentEnv.PROD;
    }
}

