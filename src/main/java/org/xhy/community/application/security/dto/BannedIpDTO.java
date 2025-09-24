package org.xhy.community.application.security.dto;

import java.time.LocalDateTime;

public class BannedIpDTO {
    private String ip;
    private LocalDateTime bannedUntil;
    private Long remainSeconds;

    public BannedIpDTO() {}

    public BannedIpDTO(String ip, LocalDateTime bannedUntil, Long remainSeconds) {
        this.ip = ip;
        this.bannedUntil = bannedUntil;
        this.remainSeconds = remainSeconds;
    }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }

    public Long getRemainSeconds() { return remainSeconds; }
    public void setRemainSeconds(Long remainSeconds) { this.remainSeconds = remainSeconds; }
}

