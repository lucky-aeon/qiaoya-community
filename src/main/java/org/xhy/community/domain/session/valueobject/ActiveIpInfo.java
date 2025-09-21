package org.xhy.community.domain.session.valueobject;

import java.time.LocalDateTime;

/**
 * 活跃IP信息值对象
 */
public class ActiveIpInfo {
    private final String ip;
    private final LocalDateTime lastSeenTime;
    private final boolean isCurrent;

    public ActiveIpInfo(String ip, LocalDateTime lastSeenTime, boolean isCurrent) {
        this.ip = ip;
        this.lastSeenTime = lastSeenTime;
        this.isCurrent = isCurrent;
    }

    public String getIp() {
        return ip;
    }

    public LocalDateTime getLastSeenTime() {
        return lastSeenTime;
    }

    public boolean isCurrent() {
        return isCurrent;
    }
}