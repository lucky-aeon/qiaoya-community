package org.xhy.community.application.security.dto;

import java.time.LocalDateTime;

public class BannedUserDTO {
    private String userId;
    private LocalDateTime bannedUntil; // 永久封禁为 null
    private Long remainSeconds;        // 永久封禁为 -1

    public BannedUserDTO() {}

    public BannedUserDTO(String userId, LocalDateTime bannedUntil, Long remainSeconds) {
        this.userId = userId;
        this.bannedUntil = bannedUntil;
        this.remainSeconds = remainSeconds;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }

    public Long getRemainSeconds() { return remainSeconds; }
    public void setRemainSeconds(Long remainSeconds) { this.remainSeconds = remainSeconds; }
}

