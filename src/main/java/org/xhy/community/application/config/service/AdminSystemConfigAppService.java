package org.xhy.community.application.config.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.xhy.community.application.config.assembler.SystemConfigAssembler;
import org.xhy.community.application.config.dto.SystemConfigDTO;
import org.xhy.community.domain.config.entity.SystemConfigEntity;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.domain.config.valueobject.UserSessionConfig;
import org.xhy.community.domain.config.valueobject.GithubOAuthConfig;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;
import org.xhy.community.infrastructure.exception.SubscriptionPlanErrorCode;

@Service
public class AdminSystemConfigAppService {

    private final SystemConfigDomainService systemConfigDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final ObjectMapper objectMapper;

    public AdminSystemConfigAppService(SystemConfigDomainService systemConfigDomainService,
                                     SubscriptionPlanDomainService subscriptionPlanDomainService,
                                     ObjectMapper objectMapper) {
        this.systemConfigDomainService = systemConfigDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据类型获取系统配置
     */
    public SystemConfigDTO getConfigByType(SystemConfigType type) {
        SystemConfigEntity entity = systemConfigDomainService.getConfigEntity(type);
        if (entity == null) {
            return new SystemConfigDTO();
        }

        // 如果是默认套餐配置，需要关联查询套餐名称
        if (type == SystemConfigType.DEFAULT_SUBSCRIPTION_PLAN) {
            return getDefaultSubscriptionConfigWithPlanName(entity);
        }

        return SystemConfigAssembler.toDTO(entity);
    }

    /**
     * 根据类型更新系统配置
     */
    public SystemConfigDTO updateConfigByType(SystemConfigType type, Object configData) {
        // 根据配置类型进行特殊验证和处理，并返回规范化后的对象用于持久化
        Object normalized = switch (type) {
            case DEFAULT_SUBSCRIPTION_PLAN -> { validateAndUpdateDefaultSubscriptionConfig(configData); yield configData; }
            case EMAIL_TEMPLATE, SYSTEM_MAINTENANCE -> { validateGeneralConfig(configData); yield configData; }
            case USER_SESSION_LIMIT -> validateAndBuildUserSessionConfig(configData);
            case OAUTH_GITHUB -> { validateAndUpdateGithubOAuthConfig(configData); yield configData; }
        };

        // 使用规范化对象进行持久化，避免未知字段导致后续解析失败
        systemConfigDomainService.updateConfigData(type, normalized);

        // 返回更新后的配置
        return getConfigByType(type);
    }

    /**
     * 获取带套餐名称的默认套餐配置
     */
    private SystemConfigDTO getDefaultSubscriptionConfigWithPlanName(SystemConfigEntity entity) {
        try {
            DefaultSubscriptionConfig config = objectMapper.readValue(entity.getData(), DefaultSubscriptionConfig.class);

            // 如果配置了套餐ID，查询套餐名称
            if (config.getSubscriptionPlanId() != null && !config.getSubscriptionPlanId().trim().isEmpty()) {
                try {
                    SubscriptionPlanEntity plan = subscriptionPlanDomainService.getSubscriptionPlanById(config.getSubscriptionPlanId());

                    // 创建扩展的配置对象，包含套餐名称
                    DefaultSubscriptionConfigWithPlanName extendedConfig = new DefaultSubscriptionConfigWithPlanName(
                        config.getSubscriptionPlanId(),
                        plan.getName()
                    );

                    return SystemConfigAssembler.toDTOWithExtendedInfo(entity, extendedConfig);
                } catch (BusinessException e) {
                    // 套餐不存在时，依然返回配置，但不包含套餐名称
                    return SystemConfigAssembler.toDTO(entity);
                }
            }

            return SystemConfigAssembler.toDTO(entity);
        } catch (Exception e) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_PARSE_ERROR,
                "解析默认套餐配置失败");
        }
    }

    /**
     * 验证并更新默认套餐配置
     */
    private void validateAndUpdateDefaultSubscriptionConfig(Object configData) {
        try {
            DefaultSubscriptionConfig config;
            if (configData instanceof DefaultSubscriptionConfig) {
                config = (DefaultSubscriptionConfig) configData;
            } else {
                // 如果是Map或其他类型，尝试转换为DefaultSubscriptionConfig
                config = objectMapper.convertValue(configData, DefaultSubscriptionConfig.class);
            }

            // 验证套餐ID是否存在（如果配置了套餐ID）
            if (config.getSubscriptionPlanId() != null && !config.getSubscriptionPlanId().trim().isEmpty()) {

                try {
                    subscriptionPlanDomainService.getSubscriptionPlanById(config.getSubscriptionPlanId());
                } catch (BusinessException e) {
                    throw new BusinessException(SubscriptionPlanErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND,
                        "指定的套餐ID不存在: " + config.getSubscriptionPlanId());
                }
            }

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                "默认套餐配置数据格式错误");
        }
    }

    /**
     * 验证通用配置
     */
    private void validateGeneralConfig(Object configData) {
        if (configData == null) {
            throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                "配置数据不能为空");
        }
    }

    /**
     * 验证并更新用户会话限制配置
     */
    private UserSessionConfig validateAndBuildUserSessionConfig(Object configData) {
        try {
            UserSessionConfig config;
            if (configData instanceof UserSessionConfig) {
                config = (UserSessionConfig) configData;
            } else {
                // 如果是Map或其他类型，尝试转换为UserSessionConfig
                config = objectMapper.convertValue(configData, UserSessionConfig.class);
            }

            // 验证配置有效性
            if (!config.isValid()) {
                throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                    "用户会话配置数据无效");
            }

            // 验证默认设备上限范围（历史字段名 maxActiveIps）
            if (config.getDefaultMaxDevices() < 1 || config.getDefaultMaxDevices() > 10) {
                throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                    "默认最大设备数必须在1-10之间");
            }

            // 验证封禁时长
            if (config.getBanTtlDays() < 0) {
                throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                    "封禁时长不能为负数");
            }

            return config;
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                "用户会话配置数据格式错误");
        }
    }

    /**
     * 验证并更新 GitHub OAuth 配置
     */
    private void validateAndUpdateGithubOAuthConfig(Object configData) {
        try {
            GithubOAuthConfig config;
            if (configData instanceof GithubOAuthConfig g) {
                config = g;
            } else {
                config = objectMapper.convertValue(configData, GithubOAuthConfig.class);
            }

            if (config == null || !config.isValid()) {
                throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                    "GitHub OAuth 配置数据无效");
            }

            // 额外验证：redirectUri 简单校验（可按需增强白名单校验）
            if (!config.getRedirectUri().startsWith("http://") && !config.getRedirectUri().startsWith("https://")) {
                throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                    "redirectUri 必须为有效的 URL");
            }

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(SystemConfigErrorCode.INVALID_CONFIG_DATA,
                "GitHub OAuth 配置数据格式错误");
        }
    }

    /**
     * 扩展的默认套餐配置，包含套餐名称
     */
    public static class DefaultSubscriptionConfigWithPlanName extends DefaultSubscriptionConfig {
        private String subscriptionPlanName;

        public DefaultSubscriptionConfigWithPlanName() {
        }

        public DefaultSubscriptionConfigWithPlanName(String subscriptionPlanId, String subscriptionPlanName) {
            super(subscriptionPlanId);
            this.subscriptionPlanName = subscriptionPlanName;
        }

        public String getSubscriptionPlanName() {
            return subscriptionPlanName;
        }

        public void setSubscriptionPlanName(String subscriptionPlanName) {
            this.subscriptionPlanName = subscriptionPlanName;
        }
    }
}
