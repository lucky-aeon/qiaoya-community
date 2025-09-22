package org.xhy.community.application.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.order.assembler.OrderAssembler;
import org.xhy.community.application.order.dto.OrderDTO;
import org.xhy.community.application.order.dto.OrderStatisticsDTO;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.query.OrderQuery;
import org.xhy.community.domain.order.service.OrderDomainService;
import org.xhy.community.domain.order.valueobject.OrderType;
import org.xhy.community.interfaces.order.request.OrderQueryRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderAppService {

    private final OrderDomainService orderDomainService;

    public AdminOrderAppService(OrderDomainService orderDomainService) {
        this.orderDomainService = orderDomainService;
    }

    /**
     * 分页查询订单
     */
    public IPage<OrderDTO> getOrdersByPage(OrderQueryRequest request) {
        OrderQuery query = OrderAssembler.toQuery(request);
        IPage<OrderEntity> orderPage = orderDomainService.getPagedOrders(query);
        return OrderAssembler.toDTOPage(orderPage);
    }

    /**
     * 根据ID获取订单详情
     */
    public OrderDTO getOrderById(String orderId) {
        OrderEntity order = orderDomainService.getOrderById(orderId);
        return OrderAssembler.toDTO(order);
    }

    /**
     * 获取订单统计信息
     * @param startTime 开始时间（可选，不传则查询所有订单）
     * @param endTime 结束时间（可选，不传则查询所有订单）
     */
    public OrderStatisticsDTO getOrderStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        OrderQuery query = new OrderQuery();
        // 只有当时间参数不为null时才设置时间条件
        if (startTime != null) {
            query.setStartTime(startTime);
        }
        if (endTime != null) {
            query.setEndTime(endTime);
        }
        query.setPageNum(1);
        query.setPageSize(Integer.MAX_VALUE);

        IPage<OrderEntity> allOrders = orderDomainService.getPagedOrders(query);
        List<OrderEntity> orders = allOrders.getRecords();

        // 统计购买订单
        long purchaseCount = orders.stream()
            .filter(order -> order.getOrderType() == OrderType.PURCHASE)
            .count();

        BigDecimal purchaseAmount = orders.stream()
            .filter(order -> order.getOrderType() == OrderType.PURCHASE)
            .map(OrderEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 统计赠送订单
        long giftCount = orders.stream()
            .filter(order -> order.getOrderType() == OrderType.GIFT)
            .count();

        return new OrderStatisticsDTO(
            orders.size(),
            purchaseCount,
            giftCount,
            purchaseAmount
        );
    }
}