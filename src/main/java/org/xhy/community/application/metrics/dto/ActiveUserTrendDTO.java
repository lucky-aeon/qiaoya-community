package org.xhy.community.application.metrics.dto;

import java.util.List;
import java.util.Map;

/**
 * 活跃用户趋势DTO
 */
public class ActiveUserTrendDTO {

    /** 总活跃用户趋势 */
    private List<TrendDataPointDTO> totalTrend;

    /** 按套餐分组的活跃用户趋势
     * Key: 套餐名称，Value: 趋势数据 */
    private Map<String, List<TrendDataPointDTO>> subscriptionTrends;

    public ActiveUserTrendDTO() {
    }

    public ActiveUserTrendDTO(List<TrendDataPointDTO> totalTrend, Map<String, List<TrendDataPointDTO>> subscriptionTrends) {
        this.totalTrend = totalTrend;
        this.subscriptionTrends = subscriptionTrends;
    }

    // Getters and Setters
    public List<TrendDataPointDTO> getTotalTrend() { return totalTrend; }
    public void setTotalTrend(List<TrendDataPointDTO> totalTrend) { this.totalTrend = totalTrend; }

    public Map<String, List<TrendDataPointDTO>> getSubscriptionTrends() { return subscriptionTrends; }
    public void setSubscriptionTrends(Map<String, List<TrendDataPointDTO>> subscriptionTrends) {
        this.subscriptionTrends = subscriptionTrends;
    }
}
