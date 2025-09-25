package org.xhy.community.domain.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.common.valueobject.AuthProvider;
import org.xhy.community.infrastructure.converter.AuthProviderConverter;

@TableName("user_social_accounts")
public class UserSocialAccountEntity extends BaseEntity {

    private String userId;

    @TableField(typeHandler = AuthProviderConverter.class)
    private AuthProvider provider;

    private String openId;

    private String login;

    private String avatarUrl;

    private String accessTokenEnc;

    private String refreshTokenEnc;

    private LocalDateTime expiresAt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }

    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getAccessTokenEnc() { return accessTokenEnc; }
    public void setAccessTokenEnc(String accessTokenEnc) { this.accessTokenEnc = accessTokenEnc; }

    public String getRefreshTokenEnc() { return refreshTokenEnc; }
    public void setRefreshTokenEnc(String refreshTokenEnc) { this.refreshTokenEnc = refreshTokenEnc; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}

