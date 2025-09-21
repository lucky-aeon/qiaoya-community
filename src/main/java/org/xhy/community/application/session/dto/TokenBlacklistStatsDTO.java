package org.xhy.community.application.session.dto;

/**
 * Token黑名单统计信息DTO
 * 用于向管理员展示黑名单的基本统计信息
 */
public class TokenBlacklistStatsDTO {

    private Long totalBlacklistedTokens;
    private String description;

    public TokenBlacklistStatsDTO() {
    }

    public TokenBlacklistStatsDTO(Long totalBlacklistedTokens, String description) {
        this.totalBlacklistedTokens = totalBlacklistedTokens;
        this.description = description;
    }

    public Long getTotalBlacklistedTokens() {
        return totalBlacklistedTokens;
    }

    public void setTotalBlacklistedTokens(Long totalBlacklistedTokens) {
        this.totalBlacklistedTokens = totalBlacklistedTokens;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}