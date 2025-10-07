package org.xhy.community.infrastructure.config;

/**
 * 部署环境枚举，仅区分 DEV 与 PROD。
 */
public enum DeploymentEnv {
    DEV,
    PROD;

    public static DeploymentEnv from(String profile) {
        if (profile == null) return PROD;
        String p = profile.trim().toLowerCase();
        if ("dev".equals(p)) return DEV;
        if ("prod".equals(p)) return PROD;
        return PROD; // 未识别时默认按生产处理
    }
}

