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
import org.xhy.community.application.subscription.dto.SimpleSubscriptionPlanDTO;
import org.xhy.community.application.subscription.assembler.SubscriptionPlanAssembler;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SubscriptionPlanErrorCode;

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
    
    public IPage<SubscriptionPlanEntity> getPagedSubscriptionPlans(int pageNum, int pageSize, String name, Integer level) {
        Page<SubscriptionPlanEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.like(StringUtils.hasText(name), SubscriptionPlanEntity::getName, name)
                   .eq(level != null, SubscriptionPlanEntity::getLevel, level);
        
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

    
    public List<SimpleSubscriptionPlanDTO> getAllSimpleSubscriptionPlans() {
        List<SubscriptionPlanEntity> entities = subscriptionPlanRepository.selectList(
            new LambdaQueryWrapper<SubscriptionPlanEntity>()
                .orderByDesc(SubscriptionPlanEntity::getCreateTime)
        );
        return entities.stream()
                      .map(SubscriptionPlanAssembler::toSimpleDTO)
                      .collect(Collectors.toList());
    }
    
    public List<String> getSubscriptionPlanCourseIds(String subscriptionPlanId) {
        List<SubscriptionPlanCourseEntity> planCourses = getSubscriptionPlanCourses(subscriptionPlanId);
        return planCourses.stream()
                         .map(SubscriptionPlanCourseEntity::getCourseId)
                         .collect(Collectors.toList());
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