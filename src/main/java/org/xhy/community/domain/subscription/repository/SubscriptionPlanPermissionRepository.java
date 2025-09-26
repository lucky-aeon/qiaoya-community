package org.xhy.community.domain.subscription.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanPermissionEntity;

@Mapper
public interface SubscriptionPlanPermissionRepository extends BaseMapper<SubscriptionPlanPermissionEntity> {
}
