package org.xhy.community.application.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OIDC UserInfo 响应 DTO
 * 仅返回授权 scope 对应的声明，键名遵循 OIDC 规范
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCUserInfoDTO {

    // 必须：当 scope 包含 openid 时至少返回 sub
    @JsonProperty("sub")
    private String sub;

    // profile 范围
    @JsonProperty("name")
    private String name;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("picture")
    private String picture;

    // email 范围
    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    // 扩展：套餐等级（自定义声明）。
    // 约定：当用户存在有效订阅时返回（无需 scope）。
    @JsonProperty("plan_level")
    private Integer planLevel;

    // Getters and Setters
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPreferredUsername() { return preferredUsername; }
    public void setPreferredUsername(String preferredUsername) { this.preferredUsername = preferredUsername; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Integer getPlanLevel() { return planLevel; }
    public void setPlanLevel(Integer planLevel) { this.planLevel = planLevel; }
}
