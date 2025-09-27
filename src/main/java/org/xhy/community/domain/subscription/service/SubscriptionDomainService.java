package org.xhy.community.domain.subscription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.repository.UserSubscriptionRepository;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanRepository;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SubscriptionErrorCode;
import org.xhy.community.infrastructure.exception.SubscriptionPlanErrorCode;
import org.xhy.community.domain.subscription.query.SubscriptionQuery;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SubscriptionDomainService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionDomainService.class);
    
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    
    public SubscriptionDomainService(UserSubscriptionRepository userSubscriptionRepository,
                                     SubscriptionPlanRepository subscriptionPlanRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }
    
    
    public UserSubscriptionEntity createSubscriptionFromCDK(String userId, String subscriptionPlanId, String cdkCode) {
        // 验证套餐存在，如果不存在会自动抛出 PLAN_NOT_FOUND 异常
        SubscriptionPlanEntity plan = getSubscriptionPlanOrThrow(subscriptionPlanId);

        // 检查重复订阅
        if (checkActiveSubscriptionExists(userId, plan.getId())) {
            throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }

        // 删除旧套餐
        userSubscriptionRepository.delete(new LambdaQueryWrapper<UserSubscriptionEntity>().eq(UserSubscriptionEntity::getUserId,userId));
        // 创建订阅记录
        UserSubscriptionEntity subscription = createSubscription(userId, plan, cdkCode);
        userSubscriptionRepository.insert(subscription);

        return subscription;
    }

    /**
     * 创建系统赠送的免费订阅（不通过CDK激活）
     *
     * @param userId 用户ID
     * @param subscriptionPlanId 套餐ID
     * @return 创建的订阅记录
     */
    public UserSubscriptionEntity createSystemGiftSubscription(String userId, String subscriptionPlanId) {
        // 验证套餐存在
        SubscriptionPlanEntity plan = getSubscriptionPlanOrThrow(subscriptionPlanId);

        try {
            // 创建系统赠送的订阅记录，cdkCode为null，使用套餐本身的有效期
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.plusMonths(plan.getValidityMonths());

            UserSubscriptionEntity subscription = new UserSubscriptionEntity(userId, plan.getId(), now, endTime, null);
            userSubscriptionRepository.insert(subscription);

            log.info("成功为用户 {} 创建系统赠送套餐: {}", userId, subscriptionPlanId);
            return subscription;

        } catch (DataIntegrityViolationException e) {
            // 唯一约束违反 = 用户已有有效套餐，这是正常情况，静默成功
            log.debug("用户 {} 已有有效套餐，跳过系统赠送套餐创建: {}", userId, subscriptionPlanId);
            return null;
        }
    }
    
    private UserSubscriptionEntity createSubscription(String userId, SubscriptionPlanEntity plan, String cdkCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMonths(plan.getValidityMonths());
        
        return new UserSubscriptionEntity(userId, plan.getId(), now, endTime, cdkCode);
    }

    private SubscriptionPlanEntity getSubscriptionPlanOrThrow(String planId) {
        SubscriptionPlanEntity plan = subscriptionPlanRepository.selectById(planId);
        if (plan == null) {
            throw new BusinessException(SubscriptionPlanErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        return plan;
    }
    
    public boolean checkActiveSubscriptionExists(String userId, String planId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper =
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .eq(UserSubscriptionEntity::getSubscriptionPlanId, planId)
                .le(UserSubscriptionEntity::getStartTime, now)
                .gt(UserSubscriptionEntity::getEndTime, now);

        return userSubscriptionRepository.exists(queryWrapper);
    }
    
    public UserSubscriptionEntity getSubscriptionById(String subscriptionId,String userId) {
        UserSubscriptionEntity subscription = userSubscriptionRepository.selectOne(
                new LambdaQueryWrapper<UserSubscriptionEntity>()
                        .eq(UserSubscriptionEntity::getId, subscriptionId)
                        .eq(UserSubscriptionEntity::getUserId, userId)
        );
        if (subscription == null) {
            throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        return subscription;
    }
    
    public List<UserSubscriptionEntity> getUserActiveSubscriptions(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper =
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .le(UserSubscriptionEntity::getStartTime, now)
                .gt(UserSubscriptionEntity::getEndTime, now)
                .orderByDesc(UserSubscriptionEntity::getCreateTime);

        return userSubscriptionRepository.selectList(queryWrapper);
    }

    public boolean hasAnyActiveSubscription(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper =
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .le(UserSubscriptionEntity::getStartTime, now)
                .gt(UserSubscriptionEntity::getEndTime, now)
                .last("LIMIT 1");

        return userSubscriptionRepository.exists(queryWrapper);
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
