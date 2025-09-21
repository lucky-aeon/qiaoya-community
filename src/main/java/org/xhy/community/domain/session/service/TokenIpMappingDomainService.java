package org.xhy.community.domain.session.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    // Redis Key 模式：user_token_ip:{userId}:{ip} -> token
    private static final String TOKEN_IP_KEY_PREFIX = "user_token_ip:";
    // Redis Key 模式：ip_tokens:{userId}:{ip} -> Set<token>
    private static final String IP_TOKENS_KEY_PREFIX = "ip_tokens:";
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

        String pattern = IP_TOKENS_KEY_PREFIX + userId + ":*";
        Set<String> keys = redis.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
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

        String pattern = IP_TOKENS_KEY_PREFIX + userId + ":*";
        Set<String> keys = redis.keys(pattern);
        if (keys != null) {
            for (String key : keys) {
                redis.opsForSet().remove(key, token);
            }
        }
    }
}