package org.xhy.community.application.metrics.dto;

import java.util.List;

/**
 * 课程趋势DTO
 */
public class CourseTrendDTO {

    /** 课程创建数趋势 */
    private List<TrendDataPointDTO> trend;

    public CourseTrendDTO() {
    }

    public CourseTrendDTO(List<TrendDataPointDTO> trend) {
        this.trend = trend;
    }

    // Getters and Setters
    public List<TrendDataPointDTO> getTrend() { return trend; }
    public void setTrend(List<TrendDataPointDTO> trend) { this.trend = trend; }
}
