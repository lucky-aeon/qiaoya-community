package org.xhy.community.infrastructure.lock;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 分布式锁抽象：提供在临界区内执行代码块的便捷方法。
 * 建议：等待时长控制在 100-300ms，租约 3-5s。
 */
public interface DistributedLock {

    /**
     * 在分布式锁的保护下执行并返回结果。
     * @param key 锁键
     * @param waitTime 获取锁的最长等待时间
     * @param leaseTime 锁租约（自动释放）
     * @param supplier 要执行的代码块
     * @return 代码块返回值
     * @throws IllegalStateException 获取锁失败或中断
     */
    <T> T executeWithLock(String key, Duration waitTime, Duration leaseTime, Supplier<T> supplier);

    /**
     * 在分布式锁的保护下执行（无返回值）。
     */
    default void runWithLock(String key, Duration waitTime, Duration leaseTime, Runnable runnable) {
        executeWithLock(key, waitTime, leaseTime, () -> {
            runnable.run();
            return null;
        });
    }
}

