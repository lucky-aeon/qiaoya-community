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

    // =============== 基于设备的并发控制（新增） ===============
    private String keyDevices(String userId) { return "u:" + userId + ":devices"; }
    private String keyDeviceIps(String userId, String deviceId) { return "u:" + userId + ":d:" + deviceId + ":ips"; }

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
     * 基于设备ID + IP 的会话创建/续活。
     * 说明：
     * - 设备并发以 deviceId 计数；同设备下容忍若干活跃 IP；
     * - 策略：当设备数超限时，按策略拒绝或淘汰最久未活跃设备；
     * - 返回 true 表示允许上线，false 表示拒绝（如被封禁或策略 DENY_NEW）。
     */
    public boolean createOrReuseByDevice(String userId, String deviceId, String ip,
                                         int maxActiveDevices, int maxIpsPerDevice, EvictPolicy policy,
                                         long sessionTtlMs, long historyWindowMs,
                                         int banThreshold, long banTtlMs) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(deviceId, "deviceId");
        Objects.requireNonNull(ip, "ip");
        Objects.requireNonNull(policy, "policy");

        return lock.executeWithLock(keyLock(userId), Duration.ofMillis(200), Duration.ofSeconds(3), () -> {
            long now = System.currentTimeMillis();
            String banKey = keyBan(userId);
            if (Boolean.TRUE.equals(redis.hasKey(banKey))) {
                return false;
            }

            String devicesKey = keyDevices(userId);
            // 先清理过期设备（根据设备 lastSeen）
            redis.opsForZSet().removeRangeByScore(devicesKey, Double.NEGATIVE_INFINITY, now - sessionTtlMs);

            // 历史滑窗（沿用 IP 维度）
            String histKey = keyHist(userId);
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

            // 设备是否已存在
            Double devScore = redis.opsForZSet().score(devicesKey, deviceId);
            if (devScore == null) {
                Long devCount = redis.opsForZSet().zCard(devicesKey);
                long count = devCount == null ? 0 : devCount;
                if (count >= maxActiveDevices) {
                    if (policy == EvictPolicy.DENY_NEW) {
                        return false;
                    }
                    // 淘汰最久未活跃设备
                    Set<ZSetOperations.TypedTuple<String>> oldest = redis.opsForZSet().rangeWithScores(devicesKey, 0, 0);
                    if (oldest != null && !oldest.isEmpty()) {
                        String victimDevice = oldest.iterator().next().getValue();
                        if (victimDevice != null) {
                            redis.opsForZSet().remove(devicesKey, victimDevice);
                            // 清理该设备的 IP 集
                            redis.delete(keyDeviceIps(userId, victimDevice));
                        }
                    }
                }
                // 加入新设备
                redis.opsForZSet().add(devicesKey, deviceId, now);
            } else {
                // 续活设备
                redis.opsForZSet().add(devicesKey, deviceId, now);
            }

            // 在设备下维护活跃 IP 集合
            String devIpsKey = keyDeviceIps(userId, deviceId);
            // 清理该设备下过期 IP
            redis.opsForZSet().removeRangeByScore(devIpsKey, Double.NEGATIVE_INFINITY, now - sessionTtlMs);

            Double ipScore = redis.opsForZSet().score(devIpsKey, ip);
            if (ipScore != null) {
                // 同设备同 IP 续活
                redis.opsForZSet().add(devIpsKey, ip, now);
                return true;
            }

            Long ipCount = redis.opsForZSet().zCard(devIpsKey);
            long ic = ipCount == null ? 0 : ipCount;
            if (ic < maxIpsPerDevice) {
                redis.opsForZSet().add(devIpsKey, ip, now);
                return true;
            }

            if (policy == EvictPolicy.DENY_NEW) {
                return false;
            }
            // 淘汰该设备下最久未活跃 IP
            Set<ZSetOperations.TypedTuple<String>> oldestIp = redis.opsForZSet().rangeWithScores(devIpsKey, 0, 0);
            if (oldestIp != null && !oldestIp.isEmpty()) {
                String victimIp = oldestIp.iterator().next().getValue();
                if (victimIp != null) {
                    redis.opsForZSet().remove(devIpsKey, victimIp);
                }
            }
            // 接纳当前 IP
            redis.opsForZSet().add(devIpsKey, ip, now);
            return true;
        });
    }

    /**
     * 判断某设备是否处于活跃状态（不校验 IP）。
     */
    public boolean isDeviceActive(String userId, String deviceId, long sessionTtlMs) {
        if (Boolean.TRUE.equals(redis.hasKey(keyBan(userId)))) {
            return false;
        }
        String devicesKey = keyDevices(userId);
        Double score = redis.opsForZSet().score(devicesKey, deviceId);
        if (score == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (score < now - sessionTtlMs) {
            redis.opsForZSet().remove(devicesKey, deviceId);
            // 同时清理该设备下 IP 集
            redis.delete(keyDeviceIps(userId, deviceId));
            return false;
        }
        return true;
    }

    /**
     * 从活跃设备集合移除一个设备（管理员或应用层主动下线某设备）。
     */
    public void removeActiveDevice(String userId, String deviceId) {
        lock.runWithLock(keyLock(userId), Duration.ofMillis(200), Duration.ofSeconds(3), () -> {
            redis.opsForZSet().remove(keyDevices(userId), deviceId);
            redis.delete(keyDeviceIps(userId, deviceId));
        });
    }



    /**
     * 检查IP是否活跃（纯读操作，无锁），按传入TTL判定过期
     * @param userId 用户ID
     * @param ip     IP地址
     * @param sessionTtlMs 会话TTL（毫秒）
     */
    public boolean isIpActive(String userId, String ip, long sessionTtlMs) {
        if (Boolean.TRUE.equals(redis.hasKey(keyBan(userId)))) {
            return false;
        }

        String activeKey = keyActive(userId);
        Double score = redis.opsForZSet().score(activeKey, ip);

        if (score == null) {
            return false;
        }

        // 懒清理：检查该IP是否已过期（按配置TTL）
        long now = System.currentTimeMillis();
        if (score < now - sessionTtlMs) {
            // IP已过期，异步移除（避免阻塞当前请求）
            redis.opsForZSet().remove(activeKey, ip);
            return false;
        }

        return true;
    }

    /**
     * 清理过期数据并进行异常检测（后台任务使用）
     * 注意：此方法不应在每次请求中调用，仅用于定时清理或管理员操作
     */
    public void cleanupExpiredData(String userId,
                                  long sessionTtlMs, long historyWindowMs,
                                  int banThreshold, long banTtlMs) {
        lock.runWithLock(keyLock(userId), Duration.ofMillis(1000), Duration.ofSeconds(3), () -> {
            long now = System.currentTimeMillis();
            String activeKey = keyActive(userId);
            String histKey = keyHist(userId);
            String banKey = keyBan(userId);

            if (Boolean.TRUE.equals(redis.hasKey(banKey))) {
                return; // 已封禁，跳过清理
            }

            // 清理过期的活跃IP
            redis.opsForZSet().removeRangeByScore(activeKey, Double.NEGATIVE_INFINITY, now - sessionTtlMs);

            // 清理过期的历史IP
            redis.opsForZSet().removeRangeByScore(histKey, Double.NEGATIVE_INFINITY, now - historyWindowMs);

            // 检查历史IP数量，进行异常检测
            Long histCount = redis.opsForZSet().zCard(histKey);
            if (histCount != null && histCount > banThreshold) {
                if (banTtlMs > 0) {
                    redis.opsForValue().set(banKey, "1", Duration.ofMillis(banTtlMs));
                } else {
                    redis.opsForValue().set(banKey, "1");
                }
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
