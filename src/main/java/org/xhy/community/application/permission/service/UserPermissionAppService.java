package org.xhy.community.application.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanCourseEntity;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.repository.SubscriptionPlanCourseRepository;
import org.xhy.community.domain.subscription.repository.UserSubscriptionRepository;
import org.xhy.community.domain.user.service.UserDomainService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户权限应用服务
 * 负责统一的权限验证逻辑，整合静态权限和动态权限
 */
@Service
public class UserPermissionAppService {
    
    private final UserDomainService userDomainService;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanCourseRepository subscriptionPlanCourseRepository;
    
    public UserPermissionAppService(UserDomainService userDomainService,
                                  UserSubscriptionRepository userSubscriptionRepository,
                                  SubscriptionPlanCourseRepository subscriptionPlanCourseRepository) {
        this.userDomainService = userDomainService;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPlanCourseRepository = subscriptionPlanCourseRepository;
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
        List<UserSubscriptionEntity> activeSubscriptions = getActiveSubscriptions(userId);
        
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
        
        LambdaQueryWrapper<UserSubscriptionEntity> queryWrapper = 
            new LambdaQueryWrapper<UserSubscriptionEntity>()
                .eq(UserSubscriptionEntity::getUserId, userId)
                .le(UserSubscriptionEntity::getStartTime, now)
                .gt(UserSubscriptionEntity::getEndTime, now);
        
        return userSubscriptionRepository.selectList(queryWrapper);
    }
    
    /**
     * 检查套餐是否包含指定课程
     */
    private boolean subscriptionIncludesCourse(String subscriptionPlanId, String courseId) {
        LambdaQueryWrapper<SubscriptionPlanCourseEntity> queryWrapper = 
            new LambdaQueryWrapper<SubscriptionPlanCourseEntity>()
                .eq(SubscriptionPlanCourseEntity::getSubscriptionPlanId, subscriptionPlanId)
                .eq(SubscriptionPlanCourseEntity::getCourseId, courseId);
        
        return subscriptionPlanCourseRepository.exists(queryWrapper);
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
}
