package org.xhy.community.domain.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * OAuth2 用户授权同意实体
 * 记录用户对客户端的授权同意
 */
@TableName("oauth2_authorization_consents")
public class OAuth2AuthorizationConsentEntity extends BaseEntity {

    /** 客户端ID */
    private String clientId;

    /** 用户标识（用户ID） */
    private String principalName;

    /** 授权的权限列表（逗号分隔的 scope） */
    private String authorities;

    /** 授权同意时间 */
    private LocalDateTime consentTime;

    // 构造函数
    public OAuth2AuthorizationConsentEntity() {
        this.consentTime = LocalDateTime.now();
    }

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public LocalDateTime getConsentTime() {
        return consentTime;
    }

    public void setConsentTime(LocalDateTime consentTime) {
        this.consentTime = consentTime;
    }
}
