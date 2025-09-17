package org.xhy.community.application.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.user.dto.UserActivityLogDTO;
import org.xhy.community.application.user.assembler.UserActivityLogAssembler;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;
import org.xhy.community.domain.log.query.UserActivityLogQuery;
import org.xhy.community.application.log.assembler.UserActivityLogQueryAssembler;
import org.xhy.community.interfaces.log.request.QueryUserActivityLogRequest;

/**
 * 管理员用户活动日志应用服务
 * 提供管理员级别的用户活动日志查询功能
 */
@Service
public class UserActivityLogAppService {
    
    private final UserActivityLogDomainService userActivityLogDomainService;
    
    public UserActivityLogAppService(UserActivityLogDomainService userActivityLogDomainService) {
        this.userActivityLogDomainService = userActivityLogDomainService;
    }
    
    /**
     * 管理员分页查询用户活动日志
     *
     * @param request 查询请求参数
     * @return 分页查询结果
     */
    public IPage<UserActivityLogDTO> getActivityLogs(QueryUserActivityLogRequest request) {
        // 转换请求对象为查询对象
        UserActivityLogQuery query = UserActivityLogQueryAssembler.toQuery(request);
        
        IPage<UserActivityLogEntity> page = userActivityLogDomainService.getActivityLogs(query);
        
        // 使用 MyBatis Plus 提供的分页结果转换
        return page.convert(UserActivityLogAssembler::toDTO);
    }
    
    /**
     * 获取指定邮箱的最近登录失败统计
     *
     * @param email 邮箱地址
     * @param hours 统计小时数
     * @return 失败次数
     */
    public Long getLoginFailureCount(String email, int hours) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().minusHours(hours);
        return userActivityLogDomainService.countLoginFailures(email, startTime);
    }
    
    /**
     * 获取指定IP的最近登录失败统计
     *
     * @param ip IP地址
     * @param hours 统计小时数
     * @return 失败次数
     */
    public Long getLoginFailureCountByIp(String ip, int hours) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().minusHours(hours);
        return userActivityLogDomainService.countLoginFailuresByIp(ip, startTime);
    }
}