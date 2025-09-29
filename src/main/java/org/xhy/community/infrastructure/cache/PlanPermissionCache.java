package org.xhy.community.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 套餐维度的权限码缓存
 * Key: perm:plan:codes:{planId}
 * Value: JSON 数组（List<String>）
 */
@Component
public class PlanPermissionCache {

    private static final Logger log = LoggerFactory.getLogger(PlanPermissionCache.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final long ttlSeconds;
    private final long emptyTtlSeconds;

    public PlanPermissionCache(StringRedisTemplate redis,
                               ObjectMapper objectMapper,
                               @Value("${community.permission.cache.plan-codes-ttl-seconds:600}") long ttlSeconds,
                               @Value("${community.permission.cache.plan-codes-empty-ttl-seconds:60}") long emptyTtlSeconds) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttlSeconds = ttlSeconds;
        this.emptyTtlSeconds = emptyTtlSeconds;
    }

    private String key(String planId) {
        return "perm:plan:codes:" + planId;
    }

    /**
     * 读取套餐权限码；未命中返回 null
     */
    public List<String> getPlanCodes(String planId) {
        try {
            String val = redis.opsForValue().get(key(planId));
            if (val == null || val.isBlank()) return null;
            return objectMapper.readValue(val, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("[PlanPermissionCache] 读取/反序列化失败，降级回源 planId={}, err={}", planId, e.getMessage());
            return null;
        }
    }

    /**
     * 写入套餐权限码缓存；空列表写短 TTL（负缓存）
     */
    public void cachePlanCodes(String planId, List<String> codes) {
        List<String> data = (codes == null ? Collections.emptyList() : codes);
        try {
            String json = objectMapper.writeValueAsString(data);
            long base = data.isEmpty() ? emptyTtlSeconds : ttlSeconds;
            Duration ttl = withJitter(Duration.ofSeconds(base));
            redis.opsForValue().set(key(planId), json, ttl);
        } catch (Exception e) {
            log.warn("[PlanPermissionCache] 写入失败，忽略缓存 planId={}, err={}", planId, e.getMessage());
        }
    }

    /**
     * 主动失效套餐权限码缓存
     */
    public void evictPlanCodes(String planId) {
        try {
            redis.delete(key(planId));
        } catch (Exception e) {
            log.warn("[PlanPermissionCache] 失效失败 planId={}, err={}", planId, e.getMessage());
        }
    }

    private Duration withJitter(Duration base) {
        long seconds = Math.max(1, base.getSeconds());
        long delta = Math.max(1, Math.round(seconds * 0.1));
        long jitter = ThreadLocalRandom.current().nextLong(-delta, delta + 1);
        long result = Math.max(1, seconds + jitter);
        return Duration.ofSeconds(result);
    }
}

