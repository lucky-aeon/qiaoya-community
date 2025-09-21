package org.xhy.community.application.config.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.config.dto.SystemConfigDTO;
import org.xhy.community.domain.config.entity.SystemConfigEntity;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.domain.config.valueobject.UserSessionConfig;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;

public class SystemConfigAssembler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SystemConfigDTO toDTO(SystemConfigEntity entity) {
        if (entity == null) {
            return null;
        }

        SystemConfigDTO dto = new SystemConfigDTO();
        BeanUtils.copyProperties(entity, dto);

        // 根据配置类型解析data字段
        try {
            Object parsedData = parseDataByType(entity.getType(), entity.getData());
            dto.setData(parsedData);
        } catch (JsonProcessingException e) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_PARSE_ERROR,
                "解析配置数据失败: " + entity.getType().name());
        }

        return dto;
    }

    public static SystemConfigDTO toDTOWithExtendedInfo(SystemConfigEntity entity, Object extendedData) {
        if (entity == null) {
            return null;
        }

        SystemConfigDTO dto = toDTO(entity);
        dto.setData(extendedData);
        return dto;
    }

    /**
     * 根据配置类型解析JSON数据
     */
    private static Object parseDataByType(SystemConfigType type, String jsonData) throws JsonProcessingException {
        return switch (type) {
            case DEFAULT_SUBSCRIPTION_PLAN -> objectMapper.readValue(jsonData, DefaultSubscriptionConfig.class);
            case USER_SESSION_LIMIT -> objectMapper.readValue(jsonData, UserSessionConfig.class);
            case EMAIL_TEMPLATE, SYSTEM_MAINTENANCE -> objectMapper.readValue(jsonData, Object.class);
        };
    }

    /**
     * 将请求数据序列化为JSON字符串
     */
    public static String serializeDataToJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_SERIALIZE_ERROR,
                "序列化配置数据失败");
        }
    }
}