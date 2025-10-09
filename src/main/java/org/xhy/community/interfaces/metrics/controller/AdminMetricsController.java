package org.xhy.community.interfaces.metrics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.metrics.dto.DashboardMetricsDTO;
import org.xhy.community.application.metrics.service.AdminMetricsAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.metrics.request.MetricsRequest;

/**
 * 管理员统计指标控制器
 * 提供仪表盘的各项统计数据
 * @module 统计分析
 */
@RestController
@RequestMapping("/api/admin/metrics")
public class AdminMetricsController {

    private final AdminMetricsAppService adminMetricsAppService;

    public AdminMetricsController(AdminMetricsAppService adminMetricsAppService) {
        this.adminMetricsAppService = adminMetricsAppService;
    }

    /**
     * 获取仪表盘所有统计指标
     * 包含：活跃用户趋势、订单趋势、注册用户趋势、课程趋势、课程学习人数（当日/周/月）
     *
     * @param request 查询请求参数
     *                - timeRange: 时间范围（DAY/WEEK/MONTH），默认DAY
     *                - days: 查询天数，默认30天
     * @return 所有统计指标数据
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardMetricsDTO> getDashboardMetrics(MetricsRequest request) {
        DashboardMetricsDTO metrics = adminMetricsAppService.getDashboardMetrics(
            request.getTimeRange(),
            request.getDays()
        );
        return ApiResponse.success(metrics);
    }
}
