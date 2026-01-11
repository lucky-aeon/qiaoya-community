package org.xhy.community.domain.codex.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.codex.valueobject.CodexConfig;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CodexErrorCode;

/**
 * Codex 配置的领域服务（Redis 存取）。
 * Key: codex:config -> JSON
 */
@Service
public class CodexConfigDomainService {
    private static final Logger log = LoggerFactory.getLogger(CodexConfigDomainService.class);
    private static final String KEY = "codex:config";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CodexConfigDomainService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public CodexConfig getConfig() {
        String json = redis.opsForValue().get(KEY);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CodexConfig.class);
        } catch (Exception e) {
            log.warn("[Codex] 配置解析失败", e);
            throw new BusinessException(CodexErrorCode.CODEX_FETCH_FAILED, "Codex 配置解析失败");
        }
    }

    public void updateConfig(CodexConfig cfg) {
        if (cfg == null) {
            throw new BusinessException(CodexErrorCode.CODEX_CONFIG_NOT_FOUND);
        }
        try {
            String json = objectMapper.writeValueAsString(cfg);
            redis.opsForValue().set(KEY, json);
        } catch (JsonProcessingException e) {
            log.warn("[Codex] 配置序列化失败", e);
            throw new BusinessException(CodexErrorCode.CODEX_FETCH_FAILED, "Codex 配置序列化失败");
        }
    }
}

