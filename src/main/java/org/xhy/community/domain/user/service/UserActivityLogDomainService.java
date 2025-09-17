package org.xhy.community.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.user.entity.UserActivityLogEntity;
import org.xhy.community.domain.user.repository.UserActivityLogRepository;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.domain.common.valueobject.AccessLevel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户活动日志领域服务
 * 负责用户活动日志的业务逻辑处理
 */
@Service
public class UserActivityLogDomainService {
    
    private final UserActivityLogRepository userActivityLogRepository;
    
    public UserActivityLogDomainService(UserActivityLogRepository userActivityLogRepository) {
        this.userActivityLogRepository = userActivityLogRepository;
    }
    
    /**
     * 记录用户活动
     *
     * @param userId 用户ID，登录失败时可能为null
     * @param email 用户邮箱
     * @param activityType 活动类型
     * @param browser 浏览器信息
     * @param equipment 设备信息
     * @param ip IP地址
     * @param userAgent User-Agent信息
     * @param failureReason 失败原因，成功时为null
     */
    public void recordActivity(String userId, String email, ActivityType activityType,
                              String browser, String equipment, String ip, 
                              String userAgent, String failureReason) {
        UserActivityLogEntity activityLog = new UserActivityLogEntity();
        activityLog.setUserId(userId);
        activityLog.setEmail(email);
        activityLog.setActivityType(activityType);
        activityLog.setBrowser(browser);
        activityLog.setEquipment(equipment);
        activityLog.setIp(ip);
        activityLog.setUserAgent(userAgent);
        activityLog.setFailureReason(failureReason);
        activityLog.setCreatedAt(LocalDateTime.now());
        activityLog.setUpdatedAt(LocalDateTime.now());
        
        userActivityLogRepository.insert(activityLog);
    }
    
    /**
     * 分页查询用户活动日志
     *
     * @param userId 用户ID，管理员查询时可以为null
     * @param email 邮箱筛选条件
     * @param activityType 活动类型筛选条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ip IP地址筛选条件
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param accessLevel 访问级别（用户级别或管理员级别）
     * @return 分页查询结果
     */
    public IPage<UserActivityLogEntity> getActivityLogs(String userId, String email, 
                                                       ActivityType activityType,
                                                       LocalDateTime startTime, LocalDateTime endTime,
                                                       String ip, Integer pageNum, Integer pageSize,
                                                       AccessLevel accessLevel) {
        Page<UserActivityLogEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = 
            new LambdaQueryWrapper<UserActivityLogEntity>()
                // 权限控制：普通用户只能查看自己的日志
                .eq(accessLevel == AccessLevel.USER && userId != null, UserActivityLogEntity::getUserId, userId)
                .like(email != null && !email.trim().isEmpty(), UserActivityLogEntity::getEmail, email)
                .eq(activityType != null, UserActivityLogEntity::getActivityType, activityType)
                .ge(startTime != null, UserActivityLogEntity::getCreatedAt, startTime)
                .le(endTime != null, UserActivityLogEntity::getCreatedAt, endTime)
                .like(ip != null && !ip.trim().isEmpty(), UserActivityLogEntity::getIp, ip)
                .orderByDesc(UserActivityLogEntity::getCreatedAt);
        
        return userActivityLogRepository.selectPage(page, queryWrapper);
    }
    
    /**
     * 统计某个邮箱的登录失败次数（指定时间范围内）
     *
     * @param email 邮箱地址
     * @param startTime 开始时间
     * @return 失败次数
     */
    public Long countLoginFailures(String email, LocalDateTime startTime) {
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = 
            new LambdaQueryWrapper<UserActivityLogEntity>()
                .eq(UserActivityLogEntity::getEmail, email)
                .eq(UserActivityLogEntity::getActivityType, ActivityType.LOGIN_FAILED)
                .ge(startTime != null, UserActivityLogEntity::getCreatedAt, startTime);
        
        return userActivityLogRepository.selectCount(queryWrapper);
    }
    
    /**
     * 统计某个IP的登录失败次数（指定时间范围内）
     *
     * @param ip IP地址
     * @param startTime 开始时间
     * @return 失败次数
     */
    public Long countLoginFailuresByIp(String ip, LocalDateTime startTime) {
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = 
            new LambdaQueryWrapper<UserActivityLogEntity>()
                .eq(UserActivityLogEntity::getIp, ip)
                .eq(UserActivityLogEntity::getActivityType, ActivityType.LOGIN_FAILED)
                .ge(startTime != null, UserActivityLogEntity::getCreatedAt, startTime);
        
        return userActivityLogRepository.selectCount(queryWrapper);
    }
    
    /**
     * 获取最近的成功登录记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近登录记录列表
     */
    public List<UserActivityLogEntity> getRecentSuccessfulLogins(String userId, int limit) {
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = 
            new LambdaQueryWrapper<UserActivityLogEntity>()
                .eq(UserActivityLogEntity::getUserId, userId)
                .eq(UserActivityLogEntity::getActivityType, ActivityType.LOGIN_SUCCESS)
                .orderByDesc(UserActivityLogEntity::getCreatedAt)
                .last("LIMIT " + limit);
        
        return userActivityLogRepository.selectList(queryWrapper);
    }
}