package org.xhy.community.domain.subscription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.repository.UserSubscriptionRepository;
import org.xhy.community.domain.subscription.valueobject.SubscriptionStatus;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SubscriptionErrorCode;
import org.xhy.community.domain.subscription.query.SubscriptionQuery;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionDomainService {
    
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    
    public SubscriptionDomainService(UserSubscriptionRepository userSubscriptionRepository,
                                   SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }
    
    
    public UserSubscriptionEntity createSubscriptionFromCDK(String userId, String subscriptionPlanId, String cdkCode) {
        // 验证套餐存在，如果不存在会自动抛出 PLAN_NOT_FOUND 异常
        SubscriptionPlanEntity plan = subscriptionPlanDomainService.getSubscriptionPlanById(subscriptionPlanId);
        
        // 检查重复订阅
        if (checkActiveSubscriptionExists(userId, plan.getId())) {
            throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }
        
        // 创建订阅记录
        UserSubscriptionEntity subscription = createSubscription(userId, plan, cdkCode);
        userSubscriptionRepository.insert(subscription);
        
        return subscription;
    }
    
    private UserSubscriptionEntity createSubscription(String userId, SubscriptionPlanEntity plan, String cdkCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMonths(plan.getValidityMonths());
        
        return new UserSubscriptionEntity(userId, plan.getId(), now, endTime, cdkCode);
    }
    
    public boolean checkActiveSubscriptionExists(String userId, String planId) {
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper = 
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .eq(UserSubscriptionEntity::getSubscriptionPlanId, planId)
                .eq(UserSubscriptionEntity::getStatus, SubscriptionStatus.ACTIVE);
        
        return userSubscriptionRepository.exists(queryWrapper);
    }
    
    public UserSubscriptionEntity getSubscriptionById(String subscriptionId,String userId) {
        UserSubscriptionEntity subscription = userSubscriptionRepository.selectOne(new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getSubscriptionPlanId, subscriptionId).eq(UserSubscriptionEntity::getUserId, userId));
        if (subscription == null) {
            throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        return subscription;
    }
    
    public List<UserSubscriptionEntity> getUserActiveSubscriptions(String userId) {
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper = 
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .eq(UserSubscriptionEntity::getStatus, SubscriptionStatus.ACTIVE)
                .orderByDesc(UserSubscriptionEntity::getCreateTime);
        
        return userSubscriptionRepository.selectList(queryWrapper);
    }
    
    public IPage<UserSubscriptionEntity> getPagedUserSubscriptions(SubscriptionQuery query) {
        Page<UserSubscriptionEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper = 
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(StringUtils.hasText(query.getUserId()), UserSubscriptionEntity::getUserId, query.getUserId())
                .orderByDesc(UserSubscriptionEntity::getCreateTime);
        
        return userSubscriptionRepository.selectPage(page, queryWrapper);
    }
}