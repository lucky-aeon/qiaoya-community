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
     * @param activityType 活动类型
     * @param browser 浏览器信息
     * @param equipment 设备信息
     * @param ip IP地址
     * @param userAgent User-Agent信息
     * @param failureReason 失败原因，成功时为null
     */
    public void recordActivity(String userId, ActivityType activityType,
                              String browser, String equipment, String ip, 
                              String userAgent, String failureReason) {
        UserActivityLogEntity activityLog = new UserActivityLogEntity();
        activityLog.setUserId(userId);
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
                // email查询现在通过context_data字段进行JSON查询 
                // 由于复杂性，暂时移除email查询条件，如需要可以后续扩展JSON查询
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
     * 统计某个用户的登录失败次数（指定时间范围内）
     * 注意：删除email字段后，改为使用userId进行统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @return 失败次数
     */
    public Long countLoginFailures(String userId, LocalDateTime startTime) {
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = 
            new LambdaQueryWrapper<UserActivityLogEntity>()
                .eq(UserActivityLogEntity::getUserId, userId)
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
    
    // ==================== 业务活动日志记录方法（新增） ====================
    
    /**
     * 记录业务活动日志
     * 用于记录用户的业务操作行为，如查看文章、发表内容等
     *
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param targetType 目标类型（如POST、COURSE、USER等）
     * @param targetId 目标对象ID
     * @param requestMethod HTTP请求方法
     * @param requestPath 请求路径
     * @param executionTimeMs 执行时间（毫秒）
     * @param ipAddress IP地址
     * @param userAgent User-Agent信息
     * @param sessionId 会话ID
     * @param requestBody 请求体内容（JSON格式）
     * @param errorMessage 错误信息，成功时为null
     * @param browser 浏览器信息
     * @param equipment 设备信息
     */
    public void recordBusinessActivity(String userId, ActivityType activityType,
                                     String targetType, String targetId,
                                     String requestMethod, String requestPath,
                                     Integer executionTimeMs, String ipAddress,
                                     String userAgent, String sessionId,
                                     String requestBody, String errorMessage,
                                     String browser, String equipment) {
        
        UserActivityLogEntity activityLog = new UserActivityLogEntity();
        
        // 设置基础字段
        activityLog.setUserId(userId);
        activityLog.setActivityType(activityType);
        
        // 设置业务扩展字段
        activityLog.setTargetType(targetType);
        activityLog.setTargetId(targetId);
        activityLog.setRequestMethod(requestMethod);
        activityLog.setRequestPath(requestPath);
        activityLog.setExecutionTimeMs(executionTimeMs);
        activityLog.setSessionId(sessionId);
        activityLog.setContextData(requestBody);
        
        // 设置网络相关字段
        activityLog.setIp(ipAddress);
        activityLog.setUserAgent(userAgent);
        
        // 设置浏览器和设备信息（现在从参数中获取，而不是设为null）
        activityLog.setBrowser(browser);
        activityLog.setEquipment(equipment);
        
        // 设置失败信息
        activityLog.setFailureReason(errorMessage);
        
        // 设置时间字段
        activityLog.setCreatedAt(LocalDateTime.now());
        activityLog.setUpdatedAt(LocalDateTime.now());
        
        userActivityLogRepository.insert(activityLog);
    }
}