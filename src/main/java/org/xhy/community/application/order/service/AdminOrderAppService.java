package org.xhy.community.application.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.application.order.assembler.OrderAssembler;
import org.xhy.community.application.order.dto.OrderDTO;
import org.xhy.community.application.order.dto.OrderStatisticsDTO;
import org.xhy.community.application.config.service.IndependentServiceAppService;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.query.OrderQuery;
import org.xhy.community.domain.order.service.OrderDomainService;
import org.xhy.community.domain.order.valueobject.OrderType;
import org.xhy.community.domain.config.valueobject.IndependentServiceConfig;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.order.request.CreateServiceOrderRequest;
import org.xhy.community.interfaces.order.request.OrderQueryRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminOrderAppService {

    private final OrderDomainService orderDomainService;
    private final UserDomainService userDomainService;
    private final IndependentServiceAppService independentServiceAppService;

    public AdminOrderAppService(OrderDomainService orderDomainService,
                                UserDomainService userDomainService,
                                IndependentServiceAppService independentServiceAppService) {
        this.orderDomainService = orderDomainService;
        this.userDomainService = userDomainService;
        this.independentServiceAppService = independentServiceAppService;
    }

    /**
     * 分页查询订单
     */
    public IPage<OrderDTO> getOrdersByPage(OrderQueryRequest request) {
        OrderQuery query = OrderAssembler.toQuery(request);
        IPage<OrderEntity> orderPage = orderDomainService.getPagedOrders(query);
        // 批量查询用户昵称并填充
        Set<String> userIds = orderPage.getRecords().stream()
                .map(OrderEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, UserEntity> userEntityMap = userDomainService.getUserEntityMapByIds(userIds);
        Map<String, String> userNameMap = userEntityMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));

        List<OrderDTO> dtos = OrderAssembler.toDTOList(orderPage.getRecords(), userNameMap);

        Page<OrderDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    /**
     * 根据ID获取订单详情
     */
    public OrderDTO getOrderById(String orderId) {
        OrderEntity order = orderDomainService.getOrderById(orderId);
        return OrderAssembler.toDTO(order);
    }

    /**
     * 创建服务订单
     */
    public OrderDTO createServiceOrder(CreateServiceOrderRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        UserEntity currentUser = StringUtils.hasText(currentUserId) ? userDomainService.getUserById(currentUserId) : null;
        String createdBy = currentUser != null ? resolveOperatorName(currentUser) : currentUserId;
        IndependentServiceConfig serviceConfig = independentServiceAppService.getEnabledService(request.getServiceCode());

        OrderEntity order = OrderAssembler.fromCreateServiceRequest(
                request,
                serviceConfig.getServiceCode(),
                serviceConfig.getTitle(),
                createdBy
        );
        OrderEntity createdOrder = orderDomainService.createOrder(order);
        return OrderAssembler.toDTO(createdOrder);
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

        long serviceCount = orders.stream()
            .filter(order -> order.getOrderType() == OrderType.SERVICE)
            .count();

        BigDecimal serviceAmount = orders.stream()
            .filter(order -> order.getOrderType() == OrderType.SERVICE)
            .map(OrderEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderStatisticsDTO(
            orders.size(),
            purchaseCount,
            giftCount,
            serviceCount,
            purchaseAmount.add(serviceAmount)
        );
    }

    private String resolveOperatorName(UserEntity user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getName())) {
            return user.getName().trim();
        }
        if (StringUtils.hasText(user.getEmail())) {
            return user.getEmail().trim();
        }
        return user.getId();
    }
}
