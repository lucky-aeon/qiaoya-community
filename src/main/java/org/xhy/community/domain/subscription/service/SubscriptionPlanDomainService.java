package org.xhy.community.domain.subscription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanCourseEntity;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanRepository;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanMenuRepository;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanPermissionRepository;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanCourseRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SubscriptionPlanErrorCode;
import org.xhy.community.domain.subscription.query.SubscriptionPlanQuery;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanMenuEntity;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanPermissionEntity;

import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;

@Service
public class SubscriptionPlanDomainService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionPlanDomainService.class);

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanCourseRepository subscriptionPlanCourseRepository;
    private final SubscriptionPlanMenuRepository subscriptionPlanMenuRepository;
    private final SubscriptionPlanPermissionRepository subscriptionPlanPermissionRepository;

    public SubscriptionPlanDomainService(SubscriptionPlanRepository subscriptionPlanRepository,
                                         SubscriptionPlanCourseRepository subscriptionPlanCourseRepository,
                                         SubscriptionPlanMenuRepository subscriptionPlanMenuRepository,
                                         SubscriptionPlanPermissionRepository subscriptionPlanPermissionRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionPlanCourseRepository = subscriptionPlanCourseRepository;
        this.subscriptionPlanMenuRepository = subscriptionPlanMenuRepository;
        this.subscriptionPlanPermissionRepository = subscriptionPlanPermissionRepository;
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
            org.slf4j.LoggerFactory.getLogger(SubscriptionPlanDomainService.class)
                    .warn("【套餐】未找到：planId={}", id);
            throw new BusinessException(SubscriptionPlanErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        return plan;
    }

    /**
     * 批量查询订阅套餐
     *
     * @param ids 套餐ID集合
     * @return 订阅套餐列表
     */
    public List<SubscriptionPlanEntity> getSubscriptionPlansByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return subscriptionPlanRepository.selectBatchIds(ids);
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
            java.util.List<SubscriptionPlanCourseEntity> list = new java.util.ArrayList<>(courseIds.size());
            for (String courseId : courseIds) {
                list.add(new SubscriptionPlanCourseEntity(subscriptionPlanId, courseId));
            }
            subscriptionPlanCourseRepository.insert(list);
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

    /**
     * 批量查询：根据多个套餐ID获取去重后的课程ID集合
     */
    public Set<String> getCourseIdsByPlanIds(Collection<String> subscriptionPlanIds) {
        if (subscriptionPlanIds == null || subscriptionPlanIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .in(SubscriptionPlanCourseEntity::getSubscriptionPlanId, subscriptionPlanIds);
        List<SubscriptionPlanCourseEntity> list = subscriptionPlanCourseRepository.selectList(queryWrapper);
        return list.stream().map(SubscriptionPlanCourseEntity::getCourseId).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 根据课程ID获取绑定该课程的所有订阅计划
     */
    public List<SubscriptionPlanEntity> getPlansByCourseId(String courseId) {
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .eq(SubscriptionPlanCourseEntity::getCourseId, courseId);
        List<SubscriptionPlanCourseEntity> bindings = subscriptionPlanCourseRepository.selectList(queryWrapper);
        if (bindings == null || bindings.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<String> planIds = bindings.stream()
                .map(SubscriptionPlanCourseEntity::getSubscriptionPlanId)
                .collect(java.util.stream.Collectors.toList());
        // 仅返回有效的套餐
        return subscriptionPlanRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubscriptionPlanEntity>()
                .in(SubscriptionPlanEntity::getId, planIds)
                .eq(SubscriptionPlanEntity::getStatus, SubscriptionPlanStatus.ACTIVE)
        );
    }

    /**
     * 批量查询：根据多个课程ID获取各课程对应的订阅计划
     * @return Map<课程ID, 订阅计划列表>
     */
    public Map<String, List<SubscriptionPlanEntity>> getPlansByCourseIds(Collection<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        // 批量查询所有课程的绑定关系
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .in(SubscriptionPlanCourseEntity::getCourseId, courseIds);
        List<SubscriptionPlanCourseEntity> bindings = subscriptionPlanCourseRepository.selectList(queryWrapper);

        if (bindings == null || bindings.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        // 提取所有套餐ID
        Set<String> planIds = bindings.stream()
                .map(SubscriptionPlanCourseEntity::getSubscriptionPlanId)
                .collect(java.util.stream.Collectors.toSet());

        // 批量查询所有有效的套餐
        Map<String, SubscriptionPlanEntity> planMap = subscriptionPlanRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubscriptionPlanEntity>()
                .in(SubscriptionPlanEntity::getId, planIds)
                .eq(SubscriptionPlanEntity::getStatus, SubscriptionPlanStatus.ACTIVE)
        ).stream().collect(java.util.stream.Collectors.toMap(SubscriptionPlanEntity::getId, p -> p));

        // 按课程ID分组
        return bindings.stream()
                .filter(b -> planMap.containsKey(b.getSubscriptionPlanId()))
                .collect(java.util.stream.Collectors.groupingBy(
                    SubscriptionPlanCourseEntity::getCourseId,
                    java.util.stream.Collectors.mapping(
                        b -> planMap.get(b.getSubscriptionPlanId()),
                        java.util.stream.Collectors.toList()
                    )
                ));
    }

    // ==================== 菜单绑定 ====================
    public void syncSubscriptionPlanMenus(String subscriptionPlanId, List<String> menuCodes) {
        // 删除现有关联
        LambdaQueryWrapper<SubscriptionPlanMenuEntity> deleteWrapper =
            new LambdaQueryWrapper<SubscriptionPlanMenuEntity>()
                .eq(SubscriptionPlanMenuEntity::getSubscriptionPlanId, subscriptionPlanId);
        subscriptionPlanMenuRepository.delete(deleteWrapper);

        // 批量插入
        if (menuCodes != null && !menuCodes.isEmpty()) {
            java.util.List<SubscriptionPlanMenuEntity> list = new java.util.ArrayList<>(menuCodes.size());
            for (String code : menuCodes) {
                list.add(new SubscriptionPlanMenuEntity(subscriptionPlanId, code));
            }
            subscriptionPlanMenuRepository.insert(list);
        }
    }

    public List<String> getSubscriptionPlanMenuCodes(String subscriptionPlanId) {
        LambdaQueryWrapper<SubscriptionPlanMenuEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanMenuEntity>()
                .eq(SubscriptionPlanMenuEntity::getSubscriptionPlanId, subscriptionPlanId);
        return subscriptionPlanMenuRepository.selectList(queryWrapper).stream()
                .map(SubscriptionPlanMenuEntity::getMenuId)
                .collect(Collectors.toList());
    }

    // ==================== 权限绑定 ====================
    public void syncSubscriptionPlanPermissions(String subscriptionPlanId, List<String> permissionCodes) {
        LambdaQueryWrapper<SubscriptionPlanPermissionEntity> deleteWrapper =
            new LambdaQueryWrapper<SubscriptionPlanPermissionEntity>()
                .eq(SubscriptionPlanPermissionEntity::getSubscriptionPlanId, subscriptionPlanId);
        subscriptionPlanPermissionRepository.delete(deleteWrapper);

        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            java.util.List<SubscriptionPlanPermissionEntity> list = new java.util.ArrayList<>(permissionCodes.size());
            for (String code : permissionCodes) {
                list.add(new SubscriptionPlanPermissionEntity(subscriptionPlanId, code));
            }
            subscriptionPlanPermissionRepository.insert(list);
        }
    }

    public List<String> getSubscriptionPlanPermissionCodes(String subscriptionPlanId) {
        LambdaQueryWrapper<SubscriptionPlanPermissionEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanPermissionEntity>()
                .eq(SubscriptionPlanPermissionEntity::getSubscriptionPlanId, subscriptionPlanId);
        return subscriptionPlanPermissionRepository.selectList(queryWrapper).stream()
                .map(SubscriptionPlanPermissionEntity::getPermissionCode)
                .collect(Collectors.toList());
    }

    public List<SubscriptionPlanEntity> getActiveSubscriptionPlans() {
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanEntity>()
                .eq(SubscriptionPlanEntity::getStatus, SubscriptionPlanStatus.ACTIVE)
                .orderByAsc(SubscriptionPlanEntity::getLevel);

        return subscriptionPlanRepository.selectList(queryWrapper);
    }

    public List<SubscriptionPlanEntity> getActivePaidSubscriptionPlans() {
        LambdaQueryWrapper<SubscriptionPlanEntity> queryWrapper =
            new LambdaQueryWrapper<SubscriptionPlanEntity>()
                .eq(SubscriptionPlanEntity::getStatus, SubscriptionPlanStatus.ACTIVE)
                .gt(SubscriptionPlanEntity::getPrice, BigDecimal.ZERO)
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
