package org.xhy.community.domain.subscription.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;

@Repository
public interface UserSubscriptionRepository extends BaseMapper<UserSubscriptionEntity> {
}