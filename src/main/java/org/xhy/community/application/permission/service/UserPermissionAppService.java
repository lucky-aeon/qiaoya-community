package org.xhy.community.application.permission.service;

import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.cache.PlanPermissionCache;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.resourcebinding.service.ResourceBindingDomainService;
import org.xhy.community.domain.resourcebinding.entity.ResourceBindingEntity;
import org.xhy.community.domain.resourcebinding.valueobject.ResourceTargetType;
import org.xhy.community.domain.course.service.ChapterDomainService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户权限应用服务
 * 负责统一的权限验证逻辑，整合静态权限和动态权限
 */
@Service
public class UserPermissionAppService {
    
    private final UserDomainService userDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final PlanPermissionCache planPermissionCache;
    private final ResourceBindingDomainService resourceBindingDomainService;
    private final ChapterDomainService chapterDomainService;
    
    public UserPermissionAppService(UserDomainService userDomainService,
                                    SubscriptionDomainService subscriptionDomainService,
                                    SubscriptionPlanDomainService subscriptionPlanDomainService,
                                    PlanPermissionCache planPermissionCache,
                                    ResourceBindingDomainService resourceBindingDomainService,
                                    ChapterDomainService chapterDomainService) {
        this.userDomainService = userDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.planPermissionCache = planPermissionCache;
        this.resourceBindingDomainService = resourceBindingDomainService;
        this.chapterDomainService = chapterDomainService;
    }
    
    /**
     * 检查用户是否有访问指定课程的权限
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否有权限访问
     */
    public boolean hasAccessToCourse(String userId, String courseId) {
        // 1. 检查静态权限：用户直接拥有的课程权限（永久有效）
        if (hasDirectCourseAccess(userId, courseId)) {
            return true;
        }
        
        // 2. 检查动态权限：通过订阅获得的课程权限（有时效性）
        return hasSubscriptionCourseAccess(userId, courseId);
    }

    /**
     * 检查用户是否对提供的任一课程拥有访问权限（直购或订阅包含）
     */
    public boolean hasAccessToAnyCourse(String userId, java.util.Set<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return false;

        // 直购集合
        java.util.Set<String> owned = new java.util.HashSet<>(userDomainService.getUserCourses(userId));
        if (!java.util.Collections.disjoint(courseIds, owned)) {
            return true;
        }

        // 订阅集合
        List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        if (actives == null || actives.isEmpty()) return false;
        java.util.Set<String> planIds = actives.stream()
                .map(UserSubscriptionEntity::getSubscriptionPlanId)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> planCourses = subscriptionPlanDomainService.getCourseIdsByPlanIds(planIds);
        return !java.util.Collections.disjoint(courseIds, planCourses);
    }

    /**
     * 判断资源下载权限
     *
     * 资源已绑定课程：
     * - 套餐功能码 RESOURCE_DOWNLOAD 存在（接口能力），并且套餐包含该资源所绑定的课程之一；
     * - 或者 用户拥有针对任一绑定课程的直购权限（等价于额外权限 RESOURCE_DOWNLOAD@COURSE）。
     *
     * 资源未绑定课程：
     * - 套餐等级1的用户：拒绝访问
     * - 其他用户（等级2+、无套餐、有直购课程）：允许访问
     */
    public boolean hasDownloadPermissionForResource(String userId, String resourceId) {
        // 解析资源绑定到的课程集合
        java.util.List<ResourceBindingEntity> bindings = resourceBindingDomainService.getBindingsByResourceId(resourceId);
        if (bindings == null || bindings.isEmpty()) {
            // 未绑定资源：拦截套餐等级1的用户，其他用户允许访问

            // 1. 获取用户当前有效订阅
            java.util.List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);

            if (actives != null && !actives.isEmpty()) {
                // 2. 获取所有订阅的套餐信息，找出最高等级
                java.util.Set<String> planIds = actives.stream()
                    .map(UserSubscriptionEntity::getSubscriptionPlanId)
                    .collect(java.util.stream.Collectors.toSet());

                List<org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity> plans =
                    subscriptionPlanDomainService.getSubscriptionPlansByIds(planIds);

                int maxLevel = plans.stream()
                    .filter(p -> p.getLevel() != null)
                    .mapToInt(org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity::getLevel)
                    .max()
                    .orElse(0);

                // 3. 如果用户最高套餐等级是1，拒绝访问
                if (maxLevel == 1) {
                    return false;
                }
            }

            // 4. 其他情况（等级2+、无套餐、有直购课程等）：允许访问
            return true;
        }

        java.util.Set<String> courseIds = new java.util.HashSet<>();
        java.util.Set<String> chapterIds = bindings.stream()
                .filter(b -> b.getTargetType() == ResourceTargetType.CHAPTER)
                .map(ResourceBindingEntity::getTargetId)
                .collect(java.util.stream.Collectors.toSet());
        if (!chapterIds.isEmpty()) {
            java.util.Map<String, String> chapterCourseMap = chapterDomainService.getChapterCourseIdMapByIds(chapterIds);
            courseIds.addAll(chapterCourseMap.values());
        }
        bindings.stream()
                .filter(b -> b.getTargetType() == ResourceTargetType.COURSE)
                .map(ResourceBindingEntity::getTargetId)
                .forEach(courseIds::add);

        if (courseIds.isEmpty()) {
            // 绑定存在但未能解析出课程，按安全默认拒绝
            return false;
        }

        // 路径一：用户对任一绑定课程拥有直购（额外权限）
        java.util.Set<String> owned = new java.util.HashSet<>(userDomainService.getUserCourses(userId));
        boolean directAllowed = !java.util.Collections.disjoint(courseIds, owned);
        if (directAllowed) return true;

        // 路径二：套餐能力 + 套餐包含课程（接口能力 + 范围能力）
        boolean hasPlanCode = hasPlanPermission(userId, "RESOURCE_DOWNLOAD");
        if (!hasPlanCode) return false;

        // 检查有效订阅套餐是否包含任一绑定课程
        java.util.List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        if (actives == null || actives.isEmpty()) return false;
        java.util.Set<String> planIds = actives.stream()
                .map(UserSubscriptionEntity::getSubscriptionPlanId)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> planCourses = subscriptionPlanDomainService.getCourseIdsByPlanIds(planIds);
        return !java.util.Collections.disjoint(courseIds, planCourses);
    }
    
    /**
     * 检查用户是否直接拥有课程权限
     */
    private boolean hasDirectCourseAccess(String userId, String courseId) {
        return userDomainService.hasUserCourse(userId, courseId);
    }
    
    /**
     * 检查用户是否通过订阅获得课程权限
     */
    private boolean hasSubscriptionCourseAccess(String userId, String courseId) {
        // 查询用户当前有效的订阅
        List<UserSubscriptionEntity> activeSubscriptions = subscriptionDomainService.getUserActiveSubscriptions(userId);
        
        if (activeSubscriptions.isEmpty()) {
            return false;
        }
        
        // 检查任何一个有效订阅的套餐是否包含该课程
        for (UserSubscriptionEntity subscription : activeSubscriptions) {
            if (subscriptionIncludesCourse(subscription.getSubscriptionPlanId(), courseId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取用户当前有效的订阅列表
     */
    private List<UserSubscriptionEntity> getActiveSubscriptions(String userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 已移至 SubscriptionDomainService
        return subscriptionDomainService.getUserActiveSubscriptions(userId);
    }
    
    /**
     * 检查套餐是否包含指定课程
     */
    private boolean subscriptionIncludesCourse(String subscriptionPlanId, String courseId) {
        return subscriptionPlanDomainService.planIncludesCourse(subscriptionPlanId, courseId);
    }
    
    /**
     * 给用户添加课程权限（用于课程CDK激活）
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    public void grantCourseAccess(String userId, String courseId) {
        userDomainService.grantCourseToUser(userId, courseId);
    }
    
    /**
     * 获取用户直接拥有的所有课程ID列表
     * 
     * @param userId 用户ID
     * @return 课程ID列表
     */
    public List<String> getDirectCoursesIds(String userId) {
        return userDomainService.getUserCourses(userId);
    }

    // ==================== 套餐功能权限（计划码） ====================
    /**
     * 获取用户的功能权限码集合（并集）
     */
    public List<String> getUserPlanPermissionCodes(String userId) {
        List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        if (actives == null || actives.isEmpty()) return List.of();
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (UserSubscriptionEntity sub : actives) {
            String planId = sub.getSubscriptionPlanId();
            List<String> codes = planPermissionCache.getPlanCodes(planId);
            if (codes == null) {
                codes = subscriptionPlanDomainService.getSubscriptionPlanPermissionCodes(planId);
                planPermissionCache.cachePlanCodes(planId, codes);
            }
            if (codes != null) set.addAll(codes);
        }
        return new java.util.ArrayList<>(set);
    }

    /**
     * 是否具备指定功能权限
     */
    public boolean hasPlanPermission(String userId, String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) return false;
        List<String> codes = getUserPlanPermissionCodes(userId);
        return codes.contains(permissionCode);
    }
}
