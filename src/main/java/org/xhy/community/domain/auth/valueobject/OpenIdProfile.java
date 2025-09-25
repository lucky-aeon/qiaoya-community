package org.xhy.community.domain.auth.valueobject;

import org.xhy.community.domain.common.valueobject.AuthProvider;

/**
 * 第三方开放平台的基础用户资料
 */
public class OpenIdProfile {
    private AuthProvider provider;
    private String openId;
    private String login;
    private String name;
    private String avatarUrl;
    private String email;
    private boolean emailVerified;
    private boolean allowMergeByEmail;

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public boolean isAllowMergeByEmail() { return allowMergeByEmail; }
    public void setAllowMergeByEmail(boolean allowMergeByEmail) { this.allowMergeByEmail = allowMergeByEmail; }
}

