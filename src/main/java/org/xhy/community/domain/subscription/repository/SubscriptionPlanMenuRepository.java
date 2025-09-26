package org.xhy.community.domain.subscription.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanMenuEntity;

@Mapper
public interface SubscriptionPlanMenuRepository extends BaseMapper<SubscriptionPlanMenuEntity> {
}
