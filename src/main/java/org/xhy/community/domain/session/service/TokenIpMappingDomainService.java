package org.xhy.community.domain.session.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Token和IP映射关系管理服务
 * 负责维护JWT token与用户IP地址的映射关系，用于设备下线时准确找到对应token
 */
@Service
public class TokenIpMappingDomainService {

    private final StringRedisTemplate redis;
    private static final Logger log = LoggerFactory.getLogger(TokenIpMappingDomainService.class);

    // Redis Key 模式：ip_tokens:{userId}:{ip} -> Set<token>
    private static final String IP_TOKENS_KEY_PREFIX = "ip_tokens:";
    // Redis Key 模式：device_tokens:{userId}:{deviceId} -> Set<token>
    private static final String DEVICE_TOKENS_KEY_PREFIX = "device_tokens:";
    private static final Duration DEFAULT_MAPPING_TTL = Duration.ofDays(1); // 映射关系默认保留1天

    public TokenIpMappingDomainService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * 建立token和IP的映射关系
     * 在用户登录成功时调用
     *
     * @param userId 用户ID
     * @param ip IP地址
     * @param token JWT token
     * @param ttl 映射关系过期时间
     */
    public void mapTokenToIp(String userId, String ip, String token, Duration ttl) {
        if (userId == null || ip == null || token == null) {
            return;
        }

        Duration actualTtl = ttl != null ? ttl : DEFAULT_MAPPING_TTL;

        // 存储IP到tokens的映射（一个IP可能对应多个token，如同一设备多次登录）
        String ipTokensKey = IP_TOKENS_KEY_PREFIX + userId + ":" + ip;
        redis.opsForSet().add(ipTokensKey, token);
        redis.expire(ipTokensKey, actualTtl);
    }

    /**
     * 建立token和IP的映射关系（使用默认过期时间）
     */
    public void mapTokenToIp(String userId, String ip, String token) {
        mapTokenToIp(userId, ip, token, null);
    }

    /**
     * 建立 token 与 deviceId 的映射（用于基于设备的即时下线）。
     */
    public void mapTokenToDevice(String userId, String deviceId, String token, Duration ttl) {
        if (userId == null || deviceId == null || token == null) {
            return;
        }
        Duration actualTtl = ttl != null ? ttl : DEFAULT_MAPPING_TTL;
        String devTokensKey = DEVICE_TOKENS_KEY_PREFIX + userId + ":" + deviceId;
        redis.opsForSet().add(devTokensKey, token);
        redis.expire(devTokensKey, actualTtl);
    }

    /**
     * 获取指定用户IP对应的所有tokens
     *
     * @param userId 用户ID
     * @param ip IP地址
     * @return token集合
     */
    public Set<String> getTokensByUserIp(String userId, String ip) {
        if (userId == null || ip == null) {
            return Set.of();
        }

        String ipTokensKey = IP_TOKENS_KEY_PREFIX + userId + ":" + ip;
        Set<String> tokens = redis.opsForSet().members(ipTokensKey);
        return tokens != null ? tokens : Set.of();
    }

    /**
     * 获取指定用户所有IP对应的所有tokens
     *
     * @param userId 用户ID
     * @return token集合
     */
    public Set<String> getAllTokensByUser(String userId) {
        if (userId == null) {
            return Set.of();
        }

        String pattern = IP_TOKENS_KEY_PREFIX + userId + ":*";
        Set<String> keys = redis.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }

        Set<String> allTokens = new HashSet<>();
        for (String key : keys) {
            Set<String> tokens = redis.opsForSet().members(key);
            if (tokens != null) {
                allTokens.addAll(tokens);
            }
        }
        return allTokens;
    }

    /** 获取指定用户设备对应的所有 tokens */
    public Set<String> getTokensByUserDevice(String userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return Set.of();
        }
        String key = DEVICE_TOKENS_KEY_PREFIX + userId + ":" + deviceId;
        Set<String> tokens = redis.opsForSet().members(key);
        return tokens != null ? tokens : Set.of();
    }

    /**
     * 移除指定用户IP的token映射
     * 在设备下线时调用
     *
     * @param userId 用户ID
     * @param ip IP地址
     */
    public void removeTokensForUserIp(String userId, String ip) {
        if (userId == null || ip == null) {
            return;
        }

        String ipTokensKey = IP_TOKENS_KEY_PREFIX + userId + ":" + ip;
        redis.delete(ipTokensKey);
        log.info("【会话】清理 IP 映射：userId={}, ip={}", userId, ip);
    }

    /**
     * 移除指定用户所有IP的token映射
     * 在用户所有设备下线时调用
     *
     * @param userId 用户ID
     */
    public void removeAllTokensForUser(String userId) {
        if (userId == null) {
            return;
        }

        String patternIp = IP_TOKENS_KEY_PREFIX + userId + ":*";
        String patternDev = DEVICE_TOKENS_KEY_PREFIX + userId + ":*";
        Set<String> keys = redis.keys(patternIp);
        Set<String> devKeys = redis.keys(patternDev);
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
        if (devKeys != null && !devKeys.isEmpty()) {
            redis.delete(devKeys);
        }
        log.info("【会话】清理所有映射：userId={}, ipKeys={}, devKeys={}", userId,
                (keys != null ? keys.size() : 0), (devKeys != null ? devKeys.size() : 0));
    }

    /**
     * 移除特定token的映射关系
     * 在token失效时调用
     *
     * @param userId 用户ID
     * @param token JWT token
     */
    public void removeSpecificToken(String userId, String token) {
        if (userId == null || token == null) {
            return;
        }

        String patternIp = IP_TOKENS_KEY_PREFIX + userId + ":*";
        String patternDev = DEVICE_TOKENS_KEY_PREFIX + userId + ":*";
        Set<String> ipKeys = redis.keys(patternIp);
        if (ipKeys != null) {
            for (String key : ipKeys) {
                redis.opsForSet().remove(key, token);
            }
        }
        Set<String> devKeys = redis.keys(patternDev);
        if (devKeys != null) {
            for (String key : devKeys) {
                redis.opsForSet().remove(key, token);
            }
        }
        log.info("【会话】清理特定 token 映射：userId={}", userId);
    }

    /** 移除指定用户某设备的 token 映射 */
    public void removeTokensForUserDevice(String userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return;
        }
        String devTokensKey = DEVICE_TOKENS_KEY_PREFIX + userId + ":" + deviceId;
        redis.delete(devTokensKey);
        log.info("【会话】清理设备映射：userId={}, deviceId={}", userId, deviceId);
    }
}
