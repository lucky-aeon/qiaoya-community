package org.xhy.community.domain.config.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.domain.config.valueobject.UserSessionConfig;

/**
 * 用户会话配置读取服务
 */
@Service
public class UserSessionConfigService {

    private final SystemConfigDomainService systemConfigDomainService;

    public UserSessionConfigService(SystemConfigDomainService systemConfigDomainService) {
        this.systemConfigDomainService = systemConfigDomainService;
    }

    /**
     * 获取用户会话限制配置
     * 如果配置不存在或解析失败，返回默认配置
     */
    public UserSessionConfig getUserSessionConfig() {
        try {
            UserSessionConfig config = systemConfigDomainService.getConfigData(
                    SystemConfigType.USER_SESSION_LIMIT, UserSessionConfig.class);

            // 如果配置不存在或无效，返回默认配置
            if (config == null || !config.isValid()) {
                return getDefaultConfig();
            }

            return config;
        } catch (Exception e) {
            // 发生异常时返回默认配置，确保系统可用性
            return getDefaultConfig();
        }
    }

    /**
     * 获取默认的用户会话配置
     */
    private UserSessionConfig getDefaultConfig() {
        return new UserSessionConfig(); // 使用默认值
    }
}