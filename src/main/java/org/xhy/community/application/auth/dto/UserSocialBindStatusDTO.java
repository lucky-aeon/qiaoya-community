package org.xhy.community.application.auth.dto;

public class UserSocialBindStatusDTO {
    private boolean bound;
    private String login;
    private String avatarUrl;
    private String provider;

    public boolean isBound() { return bound; }
    public void setBound(boolean bound) { this.bound = bound; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}

