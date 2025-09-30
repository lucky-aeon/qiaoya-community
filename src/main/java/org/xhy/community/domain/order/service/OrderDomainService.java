package org.xhy.community.domain.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.repository.OrderRepository;
import org.xhy.community.domain.order.query.OrderQuery;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OrderErrorCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderDomainService {

    private final OrderRepository orderRepository;
    private static final AtomicLong orderSequence = new AtomicLong(1); // 保留不删除，避免二方依赖变更

    public OrderDomainService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 创建订单
     */
    public OrderEntity createOrder(OrderEntity order) {
        if (order.getOrderNo() == null) {
            order.setOrderNo(generateOrderNo());
        }
        orderRepository.insert(order);
        return order;
    }

    /**
     * 根据ID获取订单
     */
    public OrderEntity getOrderById(String id) {
        OrderEntity order = orderRepository.selectById(id);
        if (order == null) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 根据CDK码获取订单
     */
    public OrderEntity getOrderByCdkCode(String cdkCode) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<OrderEntity>()
            .eq(OrderEntity::getCdkCode, cdkCode);

        return orderRepository.selectOne(queryWrapper);
    }

    /**
     * 分页查询订单
     */
    public IPage<OrderEntity> getPagedOrders(OrderQuery query) {
        Page<OrderEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(StringUtils.hasText(query.getUserId()), OrderEntity::getUserId, query.getUserId())
                   .eq(query.getOrderType() != null, OrderEntity::getOrderType, query.getOrderType())
                   .eq(query.getProductType() != null, OrderEntity::getProductType, query.getProductType())
                   .like(StringUtils.hasText(query.getProductName()), OrderEntity::getProductName, query.getProductName())
                   .like(StringUtils.hasText(query.getCdkCode()), OrderEntity::getCdkCode, query.getCdkCode())
                   .ge(query.getStartTime() != null, OrderEntity::getActivatedTime, query.getStartTime())
                   .le(query.getEndTime() != null, OrderEntity::getActivatedTime, query.getEndTime())
                   .orderByDesc(OrderEntity::getActivatedTime);

        return orderRepository.selectPage(page, queryWrapper);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 为避免多实例下序列冲突，采用时间戳 + UUID短码 组合
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String rand = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + timestamp + rand;
    }
}
