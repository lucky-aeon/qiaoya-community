package org.xhy.community.application.skill.dto;

public class SkillStatsDTO {

    private Long totalCount;

    public SkillStatsDTO() {
    }

    public SkillStatsDTO(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
}
