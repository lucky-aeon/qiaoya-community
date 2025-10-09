package org.xhy.community.domain.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;
import org.xhy.community.domain.log.repository.UserActivityLogRepository;
import org.xhy.community.domain.log.query.UserActivityLogQuery;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.domain.common.valueobject.ActivityCategory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 用户活动日志领域服务
 * 负责用户活动日志的业务逻辑处理
 */
@Service
public class UserActivityLogDomainService {

    private final UserActivityLogRepository userActivityLogRepository;
    private final ObjectMapper objectMapper;

    public UserActivityLogDomainService(UserActivityLogRepository userActivityLogRepository) {
        this.userActivityLogRepository = userActivityLogRepository;
        this.objectMapper = new ObjectMapper();
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
        activityLog.setCreateTime(LocalDateTime.now());
        activityLog.setUpdateTime(LocalDateTime.now());
        
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
                // 根据用户ID精确查询
                .eq(query.getUserId() != null, 
                    UserActivityLogEntity::getUserId, query.getUserId())
                // 精确查询单个活动类型
                .eq(query.getActivityType() != null, 
                    UserActivityLogEntity::getActivityType, query.getActivityType())
                // 分类查询多个活动类型（与activityType互斥）
                .in(query.getActivityCategory() != null, 
                    UserActivityLogEntity::getActivityType, 
                    getActivityTypesByCategory(query.getActivityCategory()))
                .ge(query.getStartTime() != null, 
                    UserActivityLogEntity::getCreateTime, query.getStartTime())
                .le(query.getEndTime() != null, 
                    UserActivityLogEntity::getCreateTime, query.getEndTime())
                .like(StringUtils.hasText(query.getIp()), 
                      UserActivityLogEntity::getIp, query.getIp())
                .orderByDesc(UserActivityLogEntity::getCreateTime);
        
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
                .ge(startTime != null, UserActivityLogEntity::getCreateTime, startTime);
        
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
                .ge(startTime != null, UserActivityLogEntity::getCreateTime, startTime);
        
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
                .orderByDesc(UserActivityLogEntity::getCreateTime)
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
        activityLog.setContextData(parseRequestBodyToMap(requestBody));
        
        // 设置网络相关字段
        activityLog.setIp(ipAddress);
        activityLog.setUserAgent(userAgent);
        
        // 设置浏览器和设备信息（现在从参数中获取，而不是设为null）
        activityLog.setBrowser(browser);
        activityLog.setEquipment(equipment);
        
        // 设置失败信息
        activityLog.setFailureReason(errorMessage);
        
        // 设置时间字段
        activityLog.setCreateTime(LocalDateTime.now());
        activityLog.setUpdateTime(LocalDateTime.now());
        
        userActivityLogRepository.insert(activityLog);
    }

    // ==================== 浏览统计（按用户去重） ====================

    /**
     * 统计某篇文章的“按用户去重”的浏览人数
     * 口径：user_activity_logs 中 activity_type = VIEW_POST 且 target_type = 'POST'
     *      对同一 user_id 去重；忽略 user_id 为空的记录
     */
    public Long getDistinctViewerCountByPostId(String postId) {
        if (!org.springframework.util.StringUtils.hasText(postId)) {
            return 0L;
        }

        // 使用条件构造器 + 分组，只选择 user_id，取结果数量即为去重人数
        LambdaQueryWrapper<UserActivityLogEntity> wrapper = new LambdaQueryWrapper<UserActivityLogEntity>()
                .eq(UserActivityLogEntity::getActivityType, ActivityType.VIEW_POST)
                .eq(UserActivityLogEntity::getTargetType, "POST")
                .eq(UserActivityLogEntity::getTargetId, postId)
                .isNotNull(UserActivityLogEntity::getUserId)
                .select(UserActivityLogEntity::getUserId)
                .groupBy(UserActivityLogEntity::getUserId);

        List<UserActivityLogEntity> list = userActivityLogRepository.selectList(wrapper);
        return (long) list.size();
    }

    /**
     * 批量统计多篇文章的“按用户去重”的浏览人数
     * 返回 Map<postId, count>
     */
    public Map<String, Long> getDistinctViewerCountMapByPostIds(Collection<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        // 选择 targetId + userId，按二者分组，返回的记录条数按 targetId 聚合后即为去重人数
        LambdaQueryWrapper<UserActivityLogEntity> wrapper = new LambdaQueryWrapper<UserActivityLogEntity>()
                .eq(UserActivityLogEntity::getActivityType, ActivityType.VIEW_POST)
                .eq(UserActivityLogEntity::getTargetType, "POST")
                .in(UserActivityLogEntity::getTargetId, postIds)
                .isNotNull(UserActivityLogEntity::getUserId)
                .select(UserActivityLogEntity::getTargetId, UserActivityLogEntity::getUserId)
                .groupBy(UserActivityLogEntity::getTargetId, UserActivityLogEntity::getUserId);

        List<UserActivityLogEntity> rows = userActivityLogRepository.selectList(wrapper);
        Map<String, Long> result = new HashMap<>();
        for (UserActivityLogEntity row : rows) {
            String pid = row.getTargetId();
            if (pid != null) {
                result.merge(pid, 1L, Long::sum);
            }
        }

        // 确保所有传入ID都有键
        for (String id : postIds) {
            result.putIfAbsent(id, 0L);
        }
        return result;
    }
    
    /**
     * 根据活动分类获取该分类下的所有活动类型
     * 用于分类查询，将分类转换为具体的活动类型列表
     *
     * @param category 活动分类
     * @return 该分类下的所有活动类型列表
     */
    private List<ActivityType> getActivityTypesByCategory(ActivityCategory category) {
        if (category == null) {
            return Arrays.asList(ActivityType.values());
        }

        return Arrays.stream(ActivityType.values())
                .filter(type -> type.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * 将请求体字符串解析为Map对象
     *
     * @param requestBody 请求体JSON字符串
     * @return Map对象，解析失败时返回包含原始字符串的Map
     */
    private Map<String, Object> parseRequestBodyToMap(String requestBody) {
        if (!StringUtils.hasText(requestBody)) {
            return null;
        }

        try {
            return objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            Map<String, Object> fallbackMap = new HashMap<>();
            fallbackMap.put("raw_content", requestBody);
            fallbackMap.put("parse_error", "Failed to parse JSON: " + e.getMessage());
            return fallbackMap;
        }
    }

    // ==================== 统计方法 ====================

    /**
     * 获取活跃用户日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活动日志列表
     */
    public List<UserActivityLogEntity> getActiveUserLogs(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<UserActivityLogEntity> queryWrapper = new LambdaQueryWrapper<UserActivityLogEntity>()
                .ge(startTime != null, UserActivityLogEntity::getCreateTime, startTime)
                .le(endTime != null, UserActivityLogEntity::getCreateTime, endTime)
                .isNotNull(UserActivityLogEntity::getUserId)
                .orderByAsc(UserActivityLogEntity::getCreateTime);

        return userActivityLogRepository.selectList(queryWrapper);
    }
}
