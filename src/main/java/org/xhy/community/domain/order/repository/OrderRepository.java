package org.xhy.community.domain.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.order.entity.OrderEntity;

@Repository
public interface OrderRepository extends BaseMapper<OrderEntity> {
}