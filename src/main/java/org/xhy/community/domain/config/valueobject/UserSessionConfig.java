package org.xhy.community.domain.config.valueobject;

import org.xhy.community.domain.session.valueobject.EvictPolicy;

import java.time.Duration;

/**
 * 用户会话限制配置
 */
public class UserSessionConfig {

    /** 默认最大并发活跃IP数（即最大设备数） */
    private Integer maxActiveIps = 1;

    /** 超配额策略：DENY_NEW | EVICT_OLDEST */
    private EvictPolicy policy = EvictPolicy.EVICT_OLDEST;

    /** 封禁时长（天）*/
    private Long banTtlDays = 7L;

    public UserSessionConfig() {
    }

    public UserSessionConfig(Integer maxActiveIps, EvictPolicy policy, Long banTtlDays) {
        this.maxActiveIps = maxActiveIps;
        this.policy = policy;
        this.banTtlDays = banTtlDays;
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        return maxActiveIps != null && maxActiveIps >= 1 && maxActiveIps <= 10
                && policy != null
                && banTtlDays != null && banTtlDays >= 0;
    }

    // ========== 代码层面控制的固定值 ==========

    /**
     * 会话TTL（30天）
     */
    public Duration getTtl() {
        return Duration.ofDays(30);
    }

    /**
     * 历史IP统计滑窗（30天）
     */
    public Duration getHistoryWindow() {
        return Duration.ofDays(30);
    }

    /**
     * 滑窗内去重IP超过阈值即封禁（10个）
     */
    public int getBanThreshold() {
        return 10;
    }

    /**
     * 续活节流间隔（60秒）
     */
    public Duration getTouchInterval() {
        return Duration.ofSeconds(60);
    }

    /**
     * 获取封禁时长
     */
    public Duration getBanTtl() {
        if (banTtlDays == 0) {
            return Duration.ZERO; // 永久封禁
        }
        return Duration.ofDays(banTtlDays);
    }

    // ========== Getters and Setters ==========

    public Integer getMaxActiveIps() {
        return maxActiveIps;
    }

    public void setMaxActiveIps(Integer maxActiveIps) {
        this.maxActiveIps = maxActiveIps;
    }

    public EvictPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(EvictPolicy policy) {
        this.policy = policy;
    }

    public Long getBanTtlDays() {
        return banTtlDays;
    }

    public void setBanTtlDays(Long banTtlDays) {
        this.banTtlDays = banTtlDays;
    }
}