package org.xhy.community.application.auth.dto;

import java.time.LocalDateTime;

public class AdminSocialAccountDTO {
    private String id;
    private String userId;
    private String userEmail;
    private String provider;
    private String login;
    private String avatarUrl;
    private LocalDateTime createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

