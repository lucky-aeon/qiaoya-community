package org.xhy.community.domain.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;
import org.xhy.community.domain.log.repository.UserActivityLogRepository;
import org.xhy.community.domain.log.query.UserActivityLogQuery;
import org.xhy.community.domain.common.valueobject.ActivityType;

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
     * 分页查询用户活动日志（管理员权限）
     * 
     * @param query 查询条件
     * @return 分页查询结果
     */
    public IPage<UserActivityLogEntity> getActivityLogs(UserActivityLogQuery query) {
        Page<UserActivityLogEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = 
            new LambdaQueryWrapper<UserActivityLogEntity>()
                .like(StringUtils.hasText(query.getEmail()), 
                      UserActivityLogEntity::getEmail, query.getEmail())
                .eq(query.getActivityType() != null, 
                    UserActivityLogEntity::getActivityType, query.getActivityType())
                .ge(query.getStartTime() != null, 
                    UserActivityLogEntity::getCreatedAt, query.getStartTime())
                .le(query.getEndTime() != null, 
                    UserActivityLogEntity::getCreatedAt, query.getEndTime())
                .like(StringUtils.hasText(query.getIp()), 
                      UserActivityLogEntity::getIp, query.getIp())
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