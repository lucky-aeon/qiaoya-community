package org.xhy.community.application.session.dto;

import java.time.LocalDateTime;

/**
 * 用户活跃会话DTO
 */
public class ActiveSessionDTO {
    private String ip;
    private LocalDateTime lastSeenTime;
    private boolean isCurrent;

    public ActiveSessionDTO() {
    }

    public ActiveSessionDTO(String ip, LocalDateTime lastSeenTime, boolean isCurrent) {
        this.ip = ip;
        this.lastSeenTime = lastSeenTime;
        this.isCurrent = isCurrent;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(LocalDateTime lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}