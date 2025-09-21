package org.xhy.community.application.session.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 被拉黑用户信息DTO
 * 用于管理员查看和管理被拉黑的用户
 */
public class BlacklistedUserDTO {

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 被拉黑时间戳 */
    private Long blacklistedAt;

    /** 被拉黑时间（格式化） */
    private LocalDateTime blacklistedTime;

    /** 被拉黑的token数量 */
    private Integer tokenCount;

    /** 被拉黑的token列表（可选，用于详细查看） */
    private List<String> blacklistedTokens;

    public BlacklistedUserDTO() {
    }

    public BlacklistedUserDTO(String userId, String username, String email, Long blacklistedAt, Integer tokenCount) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.blacklistedAt = blacklistedAt;
        this.tokenCount = tokenCount;
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

    public Long getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(Long blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public LocalDateTime getBlacklistedTime() {
        return blacklistedTime;
    }

    public void setBlacklistedTime(LocalDateTime blacklistedTime) {
        this.blacklistedTime = blacklistedTime;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public List<String> getBlacklistedTokens() {
        return blacklistedTokens;
    }

    public void setBlacklistedTokens(List<String> blacklistedTokens) {
        this.blacklistedTokens = blacklistedTokens;
    }
}