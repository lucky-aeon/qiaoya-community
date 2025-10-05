package org.xhy.community.domain.subscription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import java.time.Duration;
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
import org.xhy.community.infrastructure.lock.DistributedLock;
import org.xhy.community.domain.cdk.valueobject.CDKSubscriptionStrategy;

@Service
public class SubscriptionDomainService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionDomainService.class);
    private final DistributedLock distributedLock;
    
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    
    public SubscriptionDomainService(UserSubscriptionRepository userSubscriptionRepository,
                                     SubscriptionPlanRepository subscriptionPlanRepository,
                                     DistributedLock distributedLock) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.distributedLock = distributedLock;
    }
    
    
    public UserSubscriptionEntity createSubscriptionFromCDK(String userId, String subscriptionPlanId, String cdkCode) {
        String lockKey = "lock:user:subscription:" + userId;
        return distributedLock.executeWithLock(lockKey, Duration.ofMillis(300), Duration.ofSeconds(5), () -> {
            // 验证套餐存在，如果不存在会自动抛出 PLAN_NOT_FOUND 异常
            SubscriptionPlanEntity plan = getSubscriptionPlanOrThrow(subscriptionPlanId);

            // 兼容旧方法，默认按 PURCHASE 语义处理（替换为新订阅）
            userSubscriptionRepository.delete(new LambdaQueryWrapper<UserSubscriptionEntity>().eq(UserSubscriptionEntity::getUserId, userId));
            UserSubscriptionEntity subscription = createSubscription(userId, plan, cdkCode);
            userSubscriptionRepository.insert(subscription);
            return subscription;
        });
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

    /**
     * 套餐CDK创建订阅（支持升级/购买策略）
     */
    public UserSubscriptionEntity createSubscriptionFromCDK(String userId, String subscriptionPlanId, String cdkCode,
                                                            CDKSubscriptionStrategy strategy) {
        String lockKey = "lock:user:subscription:" + userId;
        return distributedLock.executeWithLock(lockKey, Duration.ofMillis(300), Duration.ofSeconds(5), () -> {
            SubscriptionPlanEntity newPlan = getSubscriptionPlanOrThrow(subscriptionPlanId);
            LocalDateTime now = LocalDateTime.now();

            // 查询当前有效订阅（若存在）
            UserSubscriptionEntity currentActive = getCurrentActiveSubscription(userId);

            if (currentActive == null) {
                // 无有效订阅：UPGRADE 退化为 PURCHASE
                if (strategy == CDKSubscriptionStrategy.UPGRADE) {
                    log.info("[订阅] 用户无有效订阅，UPGRADE 退化为 PURCHASE: userId={}, planId={}", userId, subscriptionPlanId);
                }
                UserSubscriptionEntity subscription = createSubscription(userId, newPlan, cdkCode);
                userSubscriptionRepository.insert(subscription);
                return subscription;
            }

            // 有有效订阅：根据策略与等级处理
            SubscriptionPlanEntity oldPlan = getSubscriptionPlanOrThrow(currentActive.getSubscriptionPlanId());

            if (strategy == CDKSubscriptionStrategy.UPGRADE) {
                if (newPlan.getLevel() <= oldPlan.getLevel()) {
                    // 同级或降级升级：报错
                    throw new BusinessException(SubscriptionErrorCode.UPGRADE_LEVEL_INVALID);
                }
                // 低->高：替换为新订阅，承接旧订阅剩余时间
                LocalDateTime endTime = currentActive.getEndTime();
                // 替换：删除旧订阅
                userSubscriptionRepository.delete(new LambdaQueryWrapper<UserSubscriptionEntity>().eq(UserSubscriptionEntity::getUserId, userId));
                UserSubscriptionEntity subscription = new UserSubscriptionEntity(userId, newPlan.getId(), now, endTime, cdkCode);
                userSubscriptionRepository.insert(subscription);
                log.info("[订阅] 升级成功：userId={}, fromLevel={} toLevel={}, carryRemaining=true, end={}",
                        userId, oldPlan.getLevel(), newPlan.getLevel(), endTime);
                return subscription;
            } else {
                // PURCHASE
                if (newPlan.getLevel() < oldPlan.getLevel()) {
                    throw new BusinessException(SubscriptionErrorCode.DOWNGRADE_PURCHASE_NOT_ALLOWED);
                }
                if (newPlan.getLevel().equals(oldPlan.getLevel())) {
                    // 同级：续费（在现有 endTime 基础上累加）
                    LocalDateTime oldEnd = currentActive.getEndTime();
                    LocalDateTime newEnd = oldEnd.plusMonths(newPlan.getValidityMonths());
                    currentActive.setEndTime(newEnd);
                    currentActive.setCdkCode(cdkCode);
                    userSubscriptionRepository.updateById(currentActive);
                    log.info("[订阅] 续费成功：userId={}, level={}, oldEnd={}, newEnd={}",
                            userId, newPlan.getLevel(), oldEnd, newEnd);
                    return currentActive;
                } else {
                    // 更高等级：替换为新订阅（不承接剩余）
                    userSubscriptionRepository.delete(new LambdaQueryWrapper<UserSubscriptionEntity>().eq(UserSubscriptionEntity::getUserId, userId));
                    UserSubscriptionEntity subscription = createSubscription(userId, newPlan, cdkCode);
                    userSubscriptionRepository.insert(subscription);
                    log.info("[订阅] 购买更高等级，替换成功：userId={}, fromLevel={} toLevel={}, end={}",
                            userId, oldPlan.getLevel(), newPlan.getLevel(), subscription.getEndTime());
                    return subscription;
                }
            }
        });
    }

    private UserSubscriptionEntity getCurrentActiveSubscription(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return userSubscriptionRepository.selectOne(
                new LambdaQueryWrapper<UserSubscriptionEntity>()
                        .eq(UserSubscriptionEntity::getUserId, userId)
                        .le(UserSubscriptionEntity::getStartTime, now)
                        .gt(UserSubscriptionEntity::getEndTime, now)
                        .orderByDesc(UserSubscriptionEntity::getEndTime)
                        .last("LIMIT 1")
        );
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
