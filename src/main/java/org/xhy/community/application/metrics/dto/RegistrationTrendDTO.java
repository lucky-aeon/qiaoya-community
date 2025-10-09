package org.xhy.community.application.metrics.dto;

import java.util.List;

/**
 * 注册用户趋势DTO
 */
public class RegistrationTrendDTO {

    /** 注册用户数趋势 */
    private List<TrendDataPointDTO> trend;

    public RegistrationTrendDTO() {
    }

    public RegistrationTrendDTO(List<TrendDataPointDTO> trend) {
        this.trend = trend;
    }

    // Getters and Setters
    public List<TrendDataPointDTO> getTrend() { return trend; }
    public void setTrend(List<TrendDataPointDTO> trend) { this.trend = trend; }
}
