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
import org.xhy.community.domain.subscription.query.SubscriptionPlanQuery;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;

import java.util.stream.Collectors;

import java.util.List;

@Service
public class SubscriptionPlanDomainService {
    
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanCourseRepository subscriptionPlanCourseRepository;

    public SubscriptionPlanDomainService(SubscriptionPlanRepository subscriptionPlanRepository,
                                       SubscriptionPlanCourseRepository subscriptionPlanCourseRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionPlanCourseRepository = subscriptionPlanCourseRepository;
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
    
    public IPage<SubscriptionPlanEntity> getPagedSubscriptionPlans(SubscriptionPlanQuery query) {
        Page<SubscriptionPlanEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.like(StringUtils.hasText(query.getName()), SubscriptionPlanEntity::getName, query.getName())
                   .eq(query.getLevel() != null, SubscriptionPlanEntity::getLevel, query.getLevel());
        
        queryWrapper.orderByDesc(SubscriptionPlanEntity::getCreateTime);
        
        return subscriptionPlanRepository.selectPage(page, queryWrapper);
    }
    
    public void syncSubscriptionPlanCourses(String subscriptionPlanId, List<String> courseIds) {
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> deleteWrapper = 
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .eq(SubscriptionPlanCourseEntity::getSubscriptionPlanId, subscriptionPlanId);
        subscriptionPlanCourseRepository.delete(deleteWrapper);
        
        if (courseIds != null && !courseIds.isEmpty()) {
            for (String courseId : courseIds) {
                SubscriptionPlanCourseEntity planCourse = 
                    new SubscriptionPlanCourseEntity(subscriptionPlanId, courseId);
                subscriptionPlanCourseRepository.insert(planCourse);
            }
        }
    }
    
    public List<SubscriptionPlanCourseEntity> getSubscriptionPlanCourses(String subscriptionPlanId) {
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> queryWrapper = 
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .eq(SubscriptionPlanCourseEntity::getSubscriptionPlanId, subscriptionPlanId);
        return subscriptionPlanCourseRepository.selectList(queryWrapper);
    }

    
    /**
     * 获取所有订阅计划实体，按创建时间倒序。
     * 分层约束：仅返回领域实体，DTO 转换在 Application 层完成。
     */
    public List<SubscriptionPlanEntity> getAllSubscriptionPlans() {
        return subscriptionPlanRepository.selectList(
            new LambdaQueryWrapper<SubscriptionPlanEntity>()
                .orderByDesc(SubscriptionPlanEntity::getCreateTime)
        );
    }

    /**
     * 校验订阅计划是否包含指定课程。
     */
    public boolean planIncludesCourse(String subscriptionPlanId, String courseId) {
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .eq(SubscriptionPlanCourseEntity::getSubscriptionPlanId, subscriptionPlanId)
                .eq(SubscriptionPlanCourseEntity::getCourseId, courseId);
        return subscriptionPlanCourseRepository.exists(queryWrapper);
    }
    
    public List<String> getSubscriptionPlanCourseIds(String subscriptionPlanId) {
        List<SubscriptionPlanCourseEntity> planCourses = getSubscriptionPlanCourses(subscriptionPlanId);
        return planCourses.stream()
                         .map(SubscriptionPlanCourseEntity::getCourseId)
                         .collect(Collectors.toList());
    }

    public List<SubscriptionPlanEntity> getActiveSubscriptionPlans() {
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanEntity>()
                .eq(SubscriptionPlanEntity::getStatus, SubscriptionPlanStatus.ACTIVE)
                .orderByAsc(SubscriptionPlanEntity::getLevel);

        return subscriptionPlanRepository.selectList(queryWrapper);
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
