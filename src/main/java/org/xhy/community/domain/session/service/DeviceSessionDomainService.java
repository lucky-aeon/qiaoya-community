package org.xhy.community.domain.session.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.session.valueobject.ActiveIpInfo;
import org.xhy.community.domain.session.valueobject.EvictPolicy;
import org.xhy.community.infrastructure.lock.DistributedLock;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 基于 IP 的“并发活跃设备”限制的领域服务。
 * 说明：
 * - 使用 Redis ZSET 维护当前活跃 IP（score=lastSeen 毫秒），用于并发与 LRU；
 * - 使用 Redis ZSET 维护滑窗历史 IP（score=最近时间），用于封禁判定；
 * - 通过分布式锁串行化同一用户的变更，避免竞态绕过配额。
 */
@Service
public class DeviceSessionDomainService {
    private final DistributedLock lock;
    private final StringRedisTemplate redis;

    public DeviceSessionDomainService(DistributedLock lock, StringRedisTemplate redis) {
        this.lock = lock;
        this.redis = redis;
    }

    private String keyActive(String userId) { return "u:" + userId + ":active_ips"; }
    private String keyHist(String userId) { return "u:" + userId + ":ip_history"; }
    private String keyBan(String userId) { return "u:" + userId + ":ban"; }
    private String keyLock(String userId) { return "lock:user:" + userId + ":ip"; }

    /**
     * 登录/上线：按策略新增或续活 IP，必要时执行 LRU 淘汰或封禁。
     * @return 是否允许上线
     */
    public boolean createOrReuseByIp(String userId, String ip,
                                     int maxActiveIps, EvictPolicy policy,
                                     long sessionTtlMs, long historyWindowMs,
                                     int banThreshold, long banTtlMs) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(ip, "ip");
        Objects.requireNonNull(policy, "policy");

        return lock.executeWithLock(keyLock(userId), Duration.ofMillis(200), Duration.ofSeconds(3), () -> {
            long now = System.currentTimeMillis();
            String activeKey = keyActive(userId);
            String histKey = keyHist(userId);
            String banKey = keyBan(userId);

            // 0) 封禁检查
            if (Boolean.TRUE.equals(redis.hasKey(banKey))) {
                return false;
            }

            // 1) 清理过期活跃 IP
            redis.opsForZSet().removeRangeByScore(activeKey, Double.NEGATIVE_INFINITY, now - sessionTtlMs);

            // 2) 历史滑窗维护与封禁判定
            redis.opsForZSet().removeRangeByScore(histKey, Double.NEGATIVE_INFINITY, now - historyWindowMs);
            redis.opsForZSet().add(histKey, ip, now);
            Long histCount = redis.opsForZSet().zCard(histKey);
            if (histCount != null && histCount > banThreshold) {
                if (banTtlMs > 0) {
                    redis.opsForValue().set(banKey, "1", Duration.ofMillis(banTtlMs));
                } else {
                    redis.opsForValue().set(banKey, "1");
                }
                return false;
            }

            // 3) 并发活跃 IP 控制
            Double score = redis.opsForZSet().score(activeKey, ip);
            if (score != null) {
                redis.opsForZSet().add(activeKey, ip, now); // 续活
                return true;
            }

            Long activeCount = redis.opsForZSet().zCard(activeKey);
            long count = activeCount == null ? 0 : activeCount;
            if (count < maxActiveIps) {
                redis.opsForZSet().add(activeKey, ip, now);
                return true;
            }

            if (policy == EvictPolicy.DENY_NEW) {
                return false;
            }

            // 4) 淘汰最久未活跃 IP，后加入新 IP
            Set<ZSetOperations.TypedTuple<String>> oldest = redis.opsForZSet().rangeWithScores(activeKey, 0, 0);
            if (oldest != null && !oldest.isEmpty()) {
                String victim = oldest.iterator().next().getValue();
                if (victim != null) {
                    redis.opsForZSet().remove(activeKey, victim);
                }
            }
            redis.opsForZSet().add(activeKey, ip, now);
            return true;
        });
    }

    /**
     * 鉴权校验：当前请求 IP 是否属于活跃集合且未被封禁。
     */
    public boolean isIpActive(String userId, String ip) {
        if (Boolean.TRUE.equals(redis.hasKey(keyBan(userId)))) {
            return false;
        }
        return redis.opsForZSet().score(keyActive(userId), ip) != null;
    }

    /**
     * 心跳/续活：刷新 lastSeen，同时做过期、滑窗与封禁维护（串行化）。
     */
    public void touchActiveIp(String userId, String ip,
                              long sessionTtlMs, long historyWindowMs,
                              int banThreshold, long banTtlMs) {
        lock.runWithLock(keyLock(userId), Duration.ofMillis(200), Duration.ofSeconds(3), () -> {
            long now = System.currentTimeMillis();
            String activeKey = keyActive(userId);
            String histKey = keyHist(userId);
            String banKey = keyBan(userId);

            if (Boolean.TRUE.equals(redis.hasKey(banKey))) {
                return; // 已封禁无需续活
            }
            // 清理与维护
            redis.opsForZSet().removeRangeByScore(activeKey, Double.NEGATIVE_INFINITY, now - sessionTtlMs);
            redis.opsForZSet().removeRangeByScore(histKey, Double.NEGATIVE_INFINITY, now - historyWindowMs);
            redis.opsForZSet().add(histKey, ip, now);
            Long histCount = redis.opsForZSet().zCard(histKey);
            if (histCount != null && histCount > banThreshold) {
                if (banTtlMs > 0) {
                    redis.opsForValue().set(banKey, "1", Duration.ofMillis(banTtlMs));
                } else {
                    redis.opsForValue().set(banKey, "1");
                }
                return;
            }

            // 若 IP 当前活跃，则续活
            if (redis.opsForZSet().score(activeKey, ip) != null) {
                redis.opsForZSet().add(activeKey, ip, now);
            }
        });
    }

    /**
     * 下线：从活跃集合移除某个 IP。
     */
    public void removeActiveIp(String userId, String ip) {
        lock.runWithLock(keyLock(userId), Duration.ofMillis(200), Duration.ofSeconds(3), () ->
                redis.opsForZSet().remove(keyActive(userId), ip));
    }

    /**
     * 获取用户活跃IP列表（带最后活跃时间）。
     */
    public List<ActiveIpInfo> getActiveIpsWithLastSeen(String userId) {
        return getActiveIpsWithLastSeen(userId, null);
    }

    /**
     * 获取用户活跃IP列表（带最后活跃时间），可标记当前IP。
     */
    public List<ActiveIpInfo> getActiveIpsWithLastSeen(String userId, String currentIp) {
        String activeKey = keyActive(userId);
        Set<ZSetOperations.TypedTuple<String>> ipsWithScores =
            redis.opsForZSet().rangeWithScores(activeKey, 0, -1);

        List<ActiveIpInfo> result = new ArrayList<>();
        if (ipsWithScores != null) {
            for (ZSetOperations.TypedTuple<String> tuple : ipsWithScores) {
                String ip = tuple.getValue();
                if (ip != null && tuple.getScore() != null) {
                    LocalDateTime lastSeenTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(tuple.getScore().longValue()),
                        ZoneId.systemDefault()
                    );
                    boolean isCurrent = ip.equals(currentIp);
                    result.add(new ActiveIpInfo(ip, lastSeenTime, isCurrent));
                }
            }
        }
        return result;
    }

    /**
     * 强制下线指定IP（管理员操作）。
     */
    public void forceRemoveActiveIp(String userId, String ip) {
        removeActiveIp(userId, ip);
    }

    /**
     * 清空用户所有活跃IP（管理员操作）。
     */
    public void clearAllActiveIps(String userId) {
        lock.runWithLock(keyLock(userId), Duration.ofMillis(200), Duration.ofSeconds(3), () ->
                redis.delete(keyActive(userId)));
    }

    /**
     * 获取用户活跃IP数量。
     */
    public int getActiveIpCount(String userId) {
        String activeKey = keyActive(userId);
        Long count = redis.opsForZSet().zCard(activeKey);
        return count == null ? 0 : count.intValue();
    }

    /**
     * 检查用户是否被封禁。
     */
    public boolean isUserBanned(String userId) {
        return Boolean.TRUE.equals(redis.hasKey(keyBan(userId)));
    }
}
