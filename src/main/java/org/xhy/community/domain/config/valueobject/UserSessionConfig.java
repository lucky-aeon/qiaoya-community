package org.xhy.community.domain.config.valueobject;

import org.xhy.community.domain.session.valueobject.EvictPolicy;

import java.time.Duration;

/**
 * 用户会话限制配置
 */
public class UserSessionConfig {

    /**
     * 系统默认的“最大并发设备数”。
     * 注意：历史字段名为 maxActiveIps（为兼容旧配置沿用该字段存储）。
     * 新口径：表示默认设备上限（defaultMaxDevices），而非“全局活跃IP上限”。
     */
    private Integer maxActiveIps = 1;

    /** 超配额策略：DENY_NEW | EVICT_OLDEST */
    private EvictPolicy policy = EvictPolicy.EVICT_OLDEST;

    /** 封禁时长（天）*/
    private Long banTtlDays = 7L;

    /**
     * 同一设备允许的活跃 IP 数上限（用于容忍梯子/网络切换）。
     * 仅在基于 deviceId 的并发控制启用时生效。
     * 默认 3。
     */
    private Integer maxIpsPerDevice = 3;

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
                && banTtlDays != null && banTtlDays >= 0
                && maxIpsPerDevice != null && maxIpsPerDevice >= 1 && maxIpsPerDevice <= 10;
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

    /** 兼容旧接口：获取历史字段 maxActiveIps（作为默认设备上限）。*/
    public Integer getMaxActiveIps() { return maxActiveIps; }
    /** 兼容旧接口：设置历史字段 maxActiveIps（作为默认设备上限）。*/
    public void setMaxActiveIps(Integer maxActiveIps) { this.maxActiveIps = maxActiveIps; }

    /** 新口径命名：默认设备上限（defaultMaxDevices） */
    public Integer getDefaultMaxDevices() { return maxActiveIps; }
    public void setDefaultMaxDevices(Integer defaultMaxDevices) { this.maxActiveIps = defaultMaxDevices; }

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

    public Integer getMaxIpsPerDevice() {
        return maxIpsPerDevice;
    }

    public void setMaxIpsPerDevice(Integer maxIpsPerDevice) {
        this.maxIpsPerDevice = maxIpsPerDevice;
    }
}
