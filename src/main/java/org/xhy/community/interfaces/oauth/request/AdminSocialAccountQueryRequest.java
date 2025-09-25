package org.xhy.community.interfaces.oauth.request;

import org.xhy.community.interfaces.common.request.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class AdminSocialAccountQueryRequest extends PageRequest {
    private String userId;
    private String provider;
    private String login;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}

