package org.xhy.community.interfaces.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.order.dto.OrderDTO;
import org.xhy.community.application.order.dto.OrderStatisticsDTO;
import org.xhy.community.application.order.service.AdminOrderAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.order.request.OrderQueryRequest;
import org.xhy.community.interfaces.order.request.OrderStatisticsRequest;

/**
 * 管理员订单管理控制器
 * 提供订单的查询、统计等管理功能，需要管理员权限
 * @module 订单管理
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderAppService adminOrderAppService;

    public AdminOrderController(AdminOrderAppService adminOrderAppService) {
        this.adminOrderAppService = adminOrderAppService;
    }

    /**
     * 分页获取订单列表
     * 支持按用户ID、订单类型、产品类型、时间范围等条件筛选
     * @param request 查询请求参数
     * @return 分页订单列表
     */
    @GetMapping
    public ApiResponse<IPage<OrderDTO>> getOrders(OrderQueryRequest request) {
        IPage<OrderDTO> orders = adminOrderAppService.getOrdersByPage(request);
        return ApiResponse.success(orders);
    }

    /**
     * 获取订单详情
     * 根据订单ID获取订单的详细信息
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> getOrderById(@PathVariable String id) {
        OrderDTO order = adminOrderAppService.getOrderById(id);
        return ApiResponse.success(order);
    }

    /**
     * 获取订单统计信息
     * 统计指定时间范围内的订单数量和金额
     * @param request 统计查询请求参数
     * @return 订单统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<OrderStatisticsDTO> getOrderStatistics(@Valid OrderStatisticsRequest request) {
        OrderStatisticsDTO statistics = adminOrderAppService.getOrderStatistics(
            request.getStartTime(), request.getEndTime());
        return ApiResponse.success(statistics);
    }
}