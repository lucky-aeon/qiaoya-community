package org.xhy.community.domain.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.config.entity.SystemConfigEntity;
import org.xhy.community.domain.config.repository.SystemConfigRepository;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;

@Service
public class SystemConfigDomainService {

    private final SystemConfigRepository systemConfigRepository;
    private final ObjectMapper objectMapper;

    public SystemConfigDomainService(SystemConfigRepository systemConfigRepository, ObjectMapper objectMapper) {
        this.systemConfigRepository = systemConfigRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取默认套餐配置
     */
    public DefaultSubscriptionConfig getDefaultSubscriptionConfig() {
        return getConfigData(SystemConfigType.DEFAULT_SUBSCRIPTION_PLAN, DefaultSubscriptionConfig.class);
    }

    /**
     * 更新默认套餐配置
     */
    public void updateDefaultSubscriptionConfig(String subscriptionPlanId) {
        DefaultSubscriptionConfig config = new DefaultSubscriptionConfig(subscriptionPlanId);
        updateConfigData(SystemConfigType.DEFAULT_SUBSCRIPTION_PLAN, config);
    }

    /**
     * 通用配置获取方法
     */
    public <T> T getConfigData(SystemConfigType type, Class<T> clazz) {
        SystemConfigEntity config = getConfigEntityPrivate(type);
        if (config == null) {
            return null;
        }

        try {
            return objectMapper.readValue(config.getData(), clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_PARSE_ERROR,
                "解析配置数据失败: " + type.name());
        }
    }

    /**
     * 通用配置更新方法
     */
    public <T> void updateConfigData(SystemConfigType type, T configData) {
        try {
            String jsonData = objectMapper.writeValueAsString(configData);

            SystemConfigEntity existingConfig = getConfigEntityPrivate(type);
            if (existingConfig != null) {
                existingConfig.setData(jsonData);
                systemConfigRepository.updateById(existingConfig);
            } else {
                SystemConfigEntity newConfig = new SystemConfigEntity(type, jsonData, type.getDescription());
                systemConfigRepository.insert(newConfig);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_SERIALIZE_ERROR,
                "序列化配置数据失败: " + type.name());
        }
    }

    /**
     * 获取配置实体
     */
    public SystemConfigEntity getConfigEntity(SystemConfigType type) {
        LambdaQueryWrapper<SystemConfigEntity> queryWrapper = new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getType, type);

        return systemConfigRepository.selectOne(queryWrapper);
    }

    /**
     * 获取配置实体（私有方法，保持向后兼容）
     */
    private SystemConfigEntity getConfigEntityPrivate(SystemConfigType type) {
        LambdaQueryWrapper<SystemConfigEntity> queryWrapper = new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getType, type);

        return systemConfigRepository.selectOne(queryWrapper);
    }
}