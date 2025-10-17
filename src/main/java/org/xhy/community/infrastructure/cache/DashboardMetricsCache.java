package org.xhy.community.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 仪表盘统计缓存（JSON 字符串）
 * key: metrics:admin:dashboard:{timeRange}:{days}
 * val: DashboardMetricsDTO 的 JSON
 */
@Component
public class DashboardMetricsCache {

    private static final Logger log = LoggerFactory.getLogger(DashboardMetricsCache.class);

    private final StringRedisTemplate redis;
    private final long ttlSeconds;

    public DashboardMetricsCache(StringRedisTemplate redis,
                                 @Value("${community.metrics.cache.dashboard-ttl-seconds:30}") long ttlSeconds) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
    }

    public String get(String timeRange, int days) {
        try {
            return redis.opsForValue().get(key(timeRange, days));
        } catch (Exception e) {
            log.warn("[MetricsCache] 读取失败 key={}, err={}", key(timeRange, days), e.getMessage());
            return null;
        }
    }

    public void set(String timeRange, int days, String json) {
        try {
            redis.opsForValue().set(key(timeRange, days), json, withJitter(Duration.ofSeconds(Math.max(1, ttlSeconds))));
        } catch (Exception e) {
            log.warn("[MetricsCache] 写入失败 key={}, err={}", key(timeRange, days), e.getMessage());
        }
    }

    private String key(String timeRange, int days) {
        return "metrics:admin:dashboard:" + timeRange + ":" + days;
    }

    private Duration withJitter(Duration base) {
        long seconds = Math.max(1, base.getSeconds());
        long delta = Math.max(1, Math.round(seconds * 0.2));
        long jitter = ThreadLocalRandom.current().nextLong(-delta, delta + 1);
        long result = Math.max(1, seconds + jitter);
        return Duration.ofSeconds(result);
    }
}

