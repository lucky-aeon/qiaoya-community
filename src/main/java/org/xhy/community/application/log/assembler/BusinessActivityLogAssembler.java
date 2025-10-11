package org.xhy.community.application.log.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;
import org.xhy.community.infrastructure.context.UserActivityContext;
import org.xhy.community.infrastructure.util.activitylog.ActivityContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务活动日志装配器
 * 将应用层收集到的上下文数据装配为领域实体，传递给 Domain 层
 */
public class BusinessActivityLogAssembler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据上下文构建 UserActivityLogEntity 实体
     *
     * @param context 应用层活动上下文
     * @param userActivityContext 用户活动上下文（浏览器/设备）
     * @param executionTimeMs 执行耗时
     * @param errorMessage 错误信息
     * @return 装配完成的实体
     */
    public static UserActivityLogEntity fromContext(ActivityContext context,
                                                    UserActivityContext userActivityContext,
                                                    int executionTimeMs,
                                                    String errorMessage) {
        if (context == null) {
            return null;
        }

        UserActivityLogEntity entity = new UserActivityLogEntity();
        // 基础字段
        entity.setUserId(context.getUserId());
        entity.setActivityType(context.getActivityType());

        // 业务扩展字段
        entity.setTargetType(context.getTargetType());
        entity.setTargetId(context.getTargetId());
        entity.setRequestMethod(context.getRequestMethod());
        entity.setRequestPath(context.getRequestPath());
        entity.setExecutionTimeMs(executionTimeMs);
        entity.setSessionId(context.getSessionId());
        entity.setContextData(parseRequestBodyToMap(context.getRequestBody()));

        // 网络及客户端信息
        entity.setIp(context.getIpAddress());
        entity.setUserAgent(context.getUserAgent());
        if (userActivityContext != null) {
            entity.setBrowser(userActivityContext.getBrowser());
            entity.setEquipment(userActivityContext.getEquipment());
        }

        // 错误信息
        entity.setFailureReason(errorMessage);

        // createTime/updateTime 由 MyBatisPlusMetaObjectHandler 统一填充
        return entity;
    }

    private static Map<String, Object> parseRequestBodyToMap(String requestBody) {
        if (requestBody == null || requestBody.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw_content", requestBody);
            fallback.put("parse_error", e.getMessage());
            return fallback;
        }
    }
}

