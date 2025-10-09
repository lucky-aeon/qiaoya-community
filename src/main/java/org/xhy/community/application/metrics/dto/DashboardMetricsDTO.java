package org.xhy.community.application.metrics.dto;

/**
 * 管理员仪表盘指标DTO
 * 包含所有统计指标数据
 */
public class DashboardMetricsDTO {

    /** 活跃用户趋势 */
    private ActiveUserTrendDTO activeUserTrend;

    /** 订单趋势 */
    private OrderTrendDTO orderTrend;

    /** 注册用户趋势 */
    private RegistrationTrendDTO registrationTrend;

    /** 课程趋势 */
    private CourseTrendDTO courseTrend;

    /** 课程学习人数指标（按当前天/周/月聚合） */
    private java.util.List<CourseLearningItemDTO> courseLearningMetrics;

    public DashboardMetricsDTO() {
    }

    public DashboardMetricsDTO(ActiveUserTrendDTO activeUserTrend, OrderTrendDTO orderTrend,
                              RegistrationTrendDTO registrationTrend, CourseTrendDTO courseTrend,
                              java.util.List<CourseLearningItemDTO> courseLearningMetrics) {
        this.activeUserTrend = activeUserTrend;
        this.orderTrend = orderTrend;
        this.registrationTrend = registrationTrend;
        this.courseTrend = courseTrend;
        this.courseLearningMetrics = courseLearningMetrics;
    }

    // Getters and Setters
    public ActiveUserTrendDTO getActiveUserTrend() { return activeUserTrend; }
    public void setActiveUserTrend(ActiveUserTrendDTO activeUserTrend) {
        this.activeUserTrend = activeUserTrend;
    }

    public OrderTrendDTO getOrderTrend() { return orderTrend; }
    public void setOrderTrend(OrderTrendDTO orderTrend) { this.orderTrend = orderTrend; }

    public RegistrationTrendDTO getRegistrationTrend() { return registrationTrend; }
    public void setRegistrationTrend(RegistrationTrendDTO registrationTrend) {
        this.registrationTrend = registrationTrend;
    }

    public CourseTrendDTO getCourseTrend() { return courseTrend; }
    public void setCourseTrend(CourseTrendDTO courseTrend) { this.courseTrend = courseTrend; }

    public java.util.List<CourseLearningItemDTO> getCourseLearningMetrics() {
        return courseLearningMetrics;
    }

    public void setCourseLearningMetrics(java.util.List<CourseLearningItemDTO> courseLearningMetrics) {
        this.courseLearningMetrics = courseLearningMetrics;
    }
}
