package org.xhy.community.interfaces.metrics.request;

import org.xhy.community.domain.common.valueobject.TimeRange;

/**
 * 统计指标查询请求
 */
public class MetricsRequest {

    /** 时间范围（DAY/WEEK/MONTH），默认按天统计 */
    private TimeRange timeRange = TimeRange.DAY;

    /** 查询天数，默认30天 */
    private Integer days = 30;

    // Getters and Setters
    public TimeRange getTimeRange() { return timeRange; }
    public void setTimeRange(TimeRange timeRange) { this.timeRange = timeRange; }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
}
