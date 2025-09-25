package org.xhy.community.domain.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.exception.AuthErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;

import java.time.Duration;

@Service
public class OAuthRateLimitDomainService {

    private final StringRedisTemplate redis;

    // Keys: oauth:github:url:ip:{ip}, oauth:github:cb:ip:{ip}
    private static final String KEY_OAUTH_URL_PREFIX = "oauth:github:url:ip:"; // ttl 60s
    private static final String KEY_OAUTH_CB_PREFIX = "oauth:github:cb:ip:";   // ttl 60s

    // limits per minute
    private static final int URL_PER_MIN_LIMIT = 20;
    private static final int CB_PER_MIN_LIMIT = 30;

    public OAuthRateLimitDomainService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void checkUrlRateLimit(String ip) {
        if (ip == null || ip.isBlank()) return;
        String key = KEY_OAUTH_URL_PREFIX + ip;
        Long cnt = redis.opsForValue().increment(key);
        if (cnt != null && cnt == 1L) {
            redis.expire(key, Duration.ofSeconds(60));
        }
        if (cnt != null && cnt > URL_PER_MIN_LIMIT) {
            throw new BusinessException(AuthErrorCode.IP_RATE_LIMIT_EXCEEDED, "请求过于频繁，请稍后再试");
        }
    }

    public void checkCallbackRateLimit(String ip) {
        if (ip == null || ip.isBlank()) return;
        String key = KEY_OAUTH_CB_PREFIX + ip;
        Long cnt = redis.opsForValue().increment(key);
        if (cnt != null && cnt == 1L) {
            redis.expire(key, Duration.ofSeconds(60));
        }
        if (cnt != null && cnt > CB_PER_MIN_LIMIT) {
            throw new BusinessException(AuthErrorCode.IP_RATE_LIMIT_EXCEEDED, "请求过于频繁，请稍后再试");
        }
    }
}

