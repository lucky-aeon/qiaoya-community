package org.xhy.community.infrastructure.lock;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 基于 Redis 的分布式锁实现：
 * - 加锁：SET key value NX PX leaseMillis
 * - 轮询等待至 waitTime 超时
 * - 解锁：Lua 脚本校验 owner 一致后 DEL
 * 说明：Lua 仅用于解锁原子性，不用于业务原子流程。
 */
@Component
public class RedisDistributedLock implements DistributedLock {
    private static final String RELEASE_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) else return 0 end";

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> releaseLua;

    public RedisDistributedLock(StringRedisTemplate redis) {
        this.redis = redis;
        this.releaseLua = new DefaultRedisScript<>();
        this.releaseLua.setScriptText(RELEASE_SCRIPT);
        this.releaseLua.setResultType(Long.class);
    }

    @Override
    public <T> T executeWithLock(String key, Duration waitTime, Duration leaseTime, Supplier<T> supplier) {
        Objects.requireNonNull(key, "lock key");
        Objects.requireNonNull(waitTime, "waitTime");
        Objects.requireNonNull(leaseTime, "leaseTime");
        Objects.requireNonNull(supplier, "supplier");

        final String owner = UUID.randomUUID().toString();
        final long deadline = System.currentTimeMillis() + waitTime.toMillis();
        final long leaseMillis = leaseTime.toMillis();

        boolean acquired = false;
        try {
            while (System.currentTimeMillis() < deadline) {
                Boolean ok = setNxPx(key, owner, leaseMillis);
                if (Boolean.TRUE.equals(ok)) {
                    acquired = true;
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("锁等待被中断: " + key, e);
                }
            }
            if (!acquired) {
                throw new IllegalStateException("获取分布式锁失败: " + key);
            }
            return supplier.get();
        } finally {
            if (acquired) {
                tryRelease(key, owner);
            }
        }
    }

    private Boolean setNxPx(String key, String val, long leaseMillis) {
        return redis.opsForValue().setIfAbsent(key, val, Duration.ofMillis(leaseMillis));
    }

    private void tryRelease(String key, String owner) {
        try {
            redis.execute(releaseLua, Collections.singletonList(key), owner);
        } catch (DataAccessException ignored) {
            // 忽略释放异常，避免影响主流程
        }
    }
}
