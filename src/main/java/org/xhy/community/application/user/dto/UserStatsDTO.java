package org.xhy.community.application.user.dto;

public class UserStatsDTO {

    private Long totalCount;

    public UserStatsDTO() {
    }

    public UserStatsDTO(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
}