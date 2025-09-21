package org.xhy.community.domain.session.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Token黑名单领域服务
 * 负责管理被下线用户的JWT token黑名单，防止被下线的token继续访问系统
 * 使用Redis Set存储黑名单token，支持自动过期清理
 */
@Service
public class TokenBlacklistDomainService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistDomainService.class);
    private final StringRedisTemplate redis;

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    private static final String USER_BLACKLIST_KEY_PREFIX = "user:blacklist:";
    private static final String BLACKLIST_USERS_KEY = "blacklist:users";
    private static final Duration DEFAULT_BLACKLIST_TTL = Duration.ofDays(1); // 黑名单token默认保留1天

    public TokenBlacklistDomainService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * 将token加入黑名单
     *
     * @param token JWT token
     * @param ttl 黑名单过期时间，如果为null则使用默认值
     */
    public void addToBlacklist(String token, Duration ttl) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + token;
        Duration actualTtl = ttl != null ? ttl : DEFAULT_BLACKLIST_TTL;

        // 设置token为黑名单状态，并设置过期时间
        redis.opsForValue().set(key, "blacklisted", actualTtl);
    }

    /**
     * 将token加入黑名单（使用默认过期时间）
     *
     * @param token JWT token
     */
    public void addToBlacklist(String token) {
        addToBlacklist(token, null);
    }

    /**
     * 批量将tokens加入黑名单
     *
     * @param tokens JWT tokens集合
     * @param ttl 黑名单过期时间，如果为null则使用默认值
     */
    public void addToBlacklist(Set<String> tokens, Duration ttl) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        Duration actualTtl = ttl != null ? ttl : DEFAULT_BLACKLIST_TTL;

        for (String token : tokens) {
            if (token != null && !token.trim().isEmpty()) {
                String key = BLACKLIST_KEY_PREFIX + token;
                redis.opsForValue().set(key, "blacklisted", actualTtl);
            }
        }
    }

    /**
     * 检查token是否在黑名单中
     *
     * @param token JWT token
     * @return true表示在黑名单中，false表示不在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String key = BLACKLIST_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    /**
     * 将token从黑名单中移除
     * 用于管理员手动恢复被误下线的用户
     *
     * @param token JWT token
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + token;
        redis.delete(key);
    }

    /**
     * 批量将tokens从黑名单中移除
     *
     * @param tokens JWT tokens集合
     */
    public void removeFromBlacklist(Set<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        for (String token : tokens) {
            if (token != null && !token.trim().isEmpty()) {
                String key = BLACKLIST_KEY_PREFIX + token;
                redis.delete(key);
            }
        }
    }

    /**
     * 将用户的tokens加入黑名单，同时维护用户级索引
     *
     * @param userId 用户ID
     * @param tokens 该用户的token集合
     * @param ttl 黑名单过期时间
     */
    public void addUserToBlacklist(String userId, Set<String> tokens, Duration ttl) {
        if (userId == null || tokens == null || tokens.isEmpty()) {
            log.warn("[黑名单添加] 参数无效: userId={}, tokensSize={}", userId, tokens != null ? tokens.size() : 0);
            return;
        }

        Duration actualTtl = ttl != null ? ttl : DEFAULT_BLACKLIST_TTL;
        long now = System.currentTimeMillis();

        log.info("[黑名单添加] 开始添加用户到黑名单: userId={}, tokensSize={}, ttl={}", userId, tokens.size(), actualTtl);

        // 1. 将tokens加入黑名单
        for (String token : tokens) {
            if (token != null && !token.trim().isEmpty()) {
                String key = BLACKLIST_KEY_PREFIX + token;
                redis.opsForValue().set(key, "blacklisted", actualTtl);
            }
        }

        // 2. 维护用户级索引：存储该用户被拉黑的token列表
        String userBlacklistKey = USER_BLACKLIST_KEY_PREFIX + userId;
        for (String token : tokens) {
            if (token != null && !token.trim().isEmpty()) {
                redis.opsForSet().add(userBlacklistKey, token);
            }
        }
        redis.expire(userBlacklistKey, actualTtl);

        // 3. 维护全局被拉黑用户列表，用于分页查询
        redis.opsForZSet().add(BLACKLIST_USERS_KEY, userId, now);

        log.info("[黑名单添加] 成功添加用户到黑名单: userId={}, userBlacklistKey={}, BLACKLIST_USERS_KEY={}",
            userId, userBlacklistKey, BLACKLIST_USERS_KEY);
    }

    /**
     * 移除指定用户的所有黑名单token
     *
     * @param userId 用户ID
     */
    public void removeUserFromBlacklist(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        String userBlacklistKey = USER_BLACKLIST_KEY_PREFIX + userId;

        // 1. 获取该用户被拉黑的所有token
        Set<String> tokens = redis.opsForSet().members(userBlacklistKey);
        if (tokens != null && !tokens.isEmpty()) {
            // 2. 从token级黑名单中移除
            removeFromBlacklist(tokens);
        }

        // 3. 清除用户级索引
        redis.delete(userBlacklistKey);

        // 4. 从全局被拉黑用户列表中移除
        redis.opsForZSet().remove(BLACKLIST_USERS_KEY, userId);
    }

    /**
     * 获取用户被拉黑的token列表
     *
     * @param userId 用户ID
     * @return 该用户被拉黑的token集合
     */
    public Set<String> getUserBlacklistTokens(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Set.of();
        }

        String userBlacklistKey = USER_BLACKLIST_KEY_PREFIX + userId;
        Set<String> tokens = redis.opsForSet().members(userBlacklistKey);
        return tokens != null ? tokens : Set.of();
    }

    /**
     * 分页获取被拉黑的用户ID列表
     *
     * @param offset 偏移量
     * @param count 数量
     * @return 用户ID列表，按拉黑时间倒序
     */
    public List<String> getBlacklistedUserIds(long offset, long count) {
        // 按分数（拉黑时间）倒序获取用户ID
        Set<String> userIds = redis.opsForZSet().reverseRange(BLACKLIST_USERS_KEY, offset, offset + count - 1);
        List<String> result = userIds != null ? userIds.stream().collect(Collectors.toList()) : List.of();
        log.info("[黑名单查询] 查询用户ID列表 offset={}, count={}, 结果数量={}, userIds={}", offset, count, result.size(), result);
        return result;
    }

    /**
     * 获取被拉黑用户总数
     *
     * @return 被拉黑用户数量
     */
    public long getBlacklistedUserCount() {
        Long count = redis.opsForZSet().zCard(BLACKLIST_USERS_KEY);
        long result = count != null ? count : 0;
        log.info("[黑名单统计] 被拉黑用户总数: {}", result);
        return result;
    }

    /**
     * 检查用户是否被拉黑
     *
     * @param userId 用户ID
     * @return true表示被拉黑，false表示未被拉黑
     */
    public boolean isUserBlacklisted(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }

        Double score = redis.opsForZSet().score(BLACKLIST_USERS_KEY, userId);
        return score != null;
    }

    /**
     * 获取用户被拉黑的时间
     *
     * @param userId 用户ID
     * @return 拉黑时间戳，如果未被拉黑则返回null
     */
    public Long getUserBlacklistTime(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        Double score = redis.opsForZSet().score(BLACKLIST_USERS_KEY, userId);
        return score != null ? score.longValue() : null;
    }

    /**
     * 清空所有黑名单token
     * 危险操作，仅供管理员使用
     */
    @Deprecated
    public void clearAllBlacklist() {
        Set<String> keys = redis.keys(BLACKLIST_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }

        // 清空用户级索引
        Set<String> userKeys = redis.keys(USER_BLACKLIST_KEY_PREFIX + "*");
        if (userKeys != null && !userKeys.isEmpty()) {
            redis.delete(userKeys);
        }

        // 清空全局用户列表
        redis.delete(BLACKLIST_USERS_KEY);
    }

    /**
     * 获取黑名单中的token数量
     *
     * @return 黑名单token数量
     */
    public long getBlacklistCount() {
        Set<String> keys = redis.keys(BLACKLIST_KEY_PREFIX + "*");
        long count = keys != null ? keys.size() : 0;
        log.info("[黑名单统计] token黑名单数量: {}, keys: {}", count, keys);
        return count;
    }
}