package org.xhy.community.application.auth.dto;

import org.xhy.community.application.user.dto.UserDTO;

public class AuthDTO {
    private String token;
    private Long expireAt;
    private boolean newUser;
    private boolean merged;
    private UserDTO user;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getExpireAt() { return expireAt; }
    public void setExpireAt(Long expireAt) { this.expireAt = expireAt; }
    public boolean isNewUser() { return newUser; }
    public void setNewUser(boolean newUser) { this.newUser = newUser; }
    public boolean isMerged() { return merged; }
    public void setMerged(boolean merged) { this.merged = merged; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}

