package org.xhy.community.domain.common.valueobject;

/**
 * 时间范围枚举
 * 用于统计指标的时间维度
 */
public enum TimeRange {

    /**
     * 按天统计
     */
    DAY("天"),

    /**
     * 按周统计
     */
    WEEK("周"),

    /**
     * 按月统计
     */
    MONTH("月");

    private final String description;

    TimeRange(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
