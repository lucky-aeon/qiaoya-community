package org.xhy.community.domain.subscription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanCourseEntity;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanRepository;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanCourseRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SubscriptionPlanErrorCode;

import java.util.List;

@Service
public class SubscriptionPlanDomainService {
    
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanDomainService(SubscriptionPlanRepository subscriptionPlanRepository
                                      ) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }
    
    public SubscriptionPlanEntity createSubscriptionPlan(SubscriptionPlanEntity plan) {
        validateUniqueSubscriptionPlanName(plan.getName(), null);
        subscriptionPlanRepository.insert(plan);
        return plan;
    }
    
    public SubscriptionPlanEntity updateSubscriptionPlan(SubscriptionPlanEntity plan) {
        validateUniqueSubscriptionPlanName(plan.getName(), plan.getId());
        subscriptionPlanRepository.updateById(plan);
        return plan;
    }
    
    public SubscriptionPlanEntity getSubscriptionPlanById(String id) {
        SubscriptionPlanEntity plan = subscriptionPlanRepository.selectById(id);
        if (plan == null) {
            throw new BusinessException(SubscriptionPlanErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        return plan;
    }
    
    public void deleteSubscriptionPlan(String id) {
        subscriptionPlanRepository.deleteById(id);
    }
    
    public IPage<SubscriptionPlanEntity> getPagedSubscriptionPlans(int pageNum, int pageSize, String name, Integer level) {
        Page<SubscriptionPlanEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.like(StringUtils.hasText(name), SubscriptionPlanEntity::getName, name)
                   .eq(level != null, SubscriptionPlanEntity::getLevel, level);
        
        queryWrapper.orderByDesc(SubscriptionPlanEntity::getCreateTime);
        
        return subscriptionPlanRepository.selectPage(page, queryWrapper);
    }

    private void validateUniqueSubscriptionPlanName(String name, String excludeId) {
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper = 
            new LambdaQueryWrapper<SubscriptionPlanEntity>()
                .eq(SubscriptionPlanEntity::getName, name);
        
        if (excludeId != null) {
            queryWrapper.ne(SubscriptionPlanEntity::getId, excludeId);
        }
        
        if (subscriptionPlanRepository.exists(queryWrapper)) {
            throw new BusinessException(SubscriptionPlanErrorCode.SUBSCRIPTION_PLAN_NAME_EXISTS);
        }
    }
}