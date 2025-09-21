package org.xhy.community.application.session.dto;

import java.util.List;

/**
 * 用户会话汇总DTO（管理员使用）
 */
public class UserSessionSummaryDTO {
    private String userId;
    private String username;
    private String email;
    private int maxDevices;
    private int activeIpCount;
    private List<ActiveSessionDTO> activeIps;
    private boolean isBanned;

    public UserSessionSummaryDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMaxDevices() {
        return maxDevices;
    }

    public void setMaxDevices(int maxDevices) {
        this.maxDevices = maxDevices;
    }

    public int getActiveIpCount() {
        return activeIpCount;
    }

    public void setActiveIpCount(int activeIpCount) {
        this.activeIpCount = activeIpCount;
    }

    public List<ActiveSessionDTO> getActiveIps() {
        return activeIps;
    }

    public void setActiveIps(List<ActiveSessionDTO> activeIps) {
        this.activeIps = activeIps;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }
}