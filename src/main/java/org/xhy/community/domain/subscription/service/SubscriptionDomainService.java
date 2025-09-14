package org.xhy.community.domain.subscription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.repository.UserSubscriptionRepository;
import org.xhy.community.domain.subscription.valueobject.SubscriptionStatus;
import org.xhy.community.domain.cdk.service.CDKDomainService;
import org.xhy.community.domain.cdk.entity.CDKEntity;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SubscriptionErrorCode;
import org.xhy.community.infrastructure.exception.CDKErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionDomainService {
    
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final CDKDomainService cdkDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CourseDomainService courseDomainService;
    
    public SubscriptionDomainService(UserSubscriptionRepository userSubscriptionRepository,
                                   CDKDomainService cdkDomainService,
                                   SubscriptionPlanDomainService subscriptionPlanDomainService,
                                   CourseDomainService courseDomainService) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.cdkDomainService = cdkDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.courseDomainService = courseDomainService;
    }
    
    public CDKActivationResult activateCDK(String userId, String cdkCode) {
        // 1. 验证CDK有效性
        CDKEntity cdk;
        try {
            cdk = cdkDomainService.getCDKByCode(cdkCode);
        } catch (BusinessException e) {
            throw e; // 直接抛出CDK验证异常
        }
        
        if (!cdk.isUsable()) {
            throw new BusinessException(CDKErrorCode.CDK_NOT_USABLE);
        }
        
        // 2. 根据CDK类型处理
        if (cdk.getCdkType() == CDKType.SUBSCRIPTION_PLAN) {
            return activateSubscriptionPlanCDK(userId, cdk);
        } else if (cdk.getCdkType() == CDKType.COURSE) {
            return activateCourseCDK(userId, cdk);
        } else {
            throw new BusinessException(CDKErrorCode.INVALID_CDK_TYPE);
        }
    }
    
    private CDKActivationResult activateSubscriptionPlanCDK(String userId, CDKEntity cdk) {
        // 验证套餐存在
        SubscriptionPlanEntity plan;
        try {
            plan = subscriptionPlanDomainService.getSubscriptionPlanById(cdk.getTargetId());
        } catch (BusinessException e) {
            throw new BusinessException(SubscriptionErrorCode.PLAN_NOT_FOUND_FOR_CDK);
        }
        
        // 检查重复订阅
        if (checkActiveSubscriptionExists(userId, plan.getId())) {
            throw new BusinessException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }
        
        // 创建订阅记录
        UserSubscriptionEntity subscription = createSubscription(userId, plan, cdk.getCode());
        userSubscriptionRepository.insert(subscription);
        
        // 标记CDK已使用
        cdkDomainService.markCDKAsUsed(cdk.getCode(), userId);
        
        return new CDKActivationResult(true, "套餐激活成功", subscription, plan.getName());
    }
    
    private CDKActivationResult activateCourseCDK(String userId, CDKEntity cdk) {
        // 验证课程存在
        try {
            courseDomainService.getCourseById(cdk.getTargetId());
        } catch (BusinessException e) {
            throw new BusinessException(SubscriptionErrorCode.COURSE_NOT_FOUND_FOR_CDK);
        }
        
        // 标记CDK已使用
        cdkDomainService.markCDKAsUsed(cdk.getCode(), userId);
        
        // 课程CDK激活成功（权限处理留给将来的权限领域）
        return new CDKActivationResult(true, "课程激活成功", null, "课程权限");
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
    
    public IPage<UserSubscriptionEntity> getPagedUserSubscriptions(String userId, int pageNum, int pageSize) {
        Page<UserSubscriptionEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper = 
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .orderByDesc(UserSubscriptionEntity::getCreateTime);
        
        return userSubscriptionRepository.selectPage(page, queryWrapper);
    }
    
    // CDK激活结果内部类
    public static class CDKActivationResult {
        private final boolean success;
        private final String message;
        private final UserSubscriptionEntity subscription;
        private final String targetName;
        
        public CDKActivationResult(boolean success, String message, UserSubscriptionEntity subscription, String targetName) {
            this.success = success;
            this.message = message;
            this.subscription = subscription;
            this.targetName = targetName;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserSubscriptionEntity getSubscription() { return subscription; }
        public String getTargetName() { return targetName; }
    }
}