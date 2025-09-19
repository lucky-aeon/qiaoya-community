package org.xhy.community.interfaces.log.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.xhy.community.application.log.service.UserActivityLogAppService;
import org.xhy.community.application.user.dto.UserActivityLogDTO;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.log.request.QueryUserActivityLogRequest;

/**
 * 管理员用户活动日志控制器
 * 提供管理员查看用户活动日志的统一接口
 */
@RestController
@RequestMapping("/api/admin/user-activity-logs")
public class AdminUserActivityLogController {
    
    private final UserActivityLogAppService userActivityLogAppService;
    
    public AdminUserActivityLogController(UserActivityLogAppService userActivityLogAppService) {
        this.userActivityLogAppService = userActivityLogAppService;
    }
    
    /**
     * 查询用户活动日志接口
     * 支持详细日志查询，通过参数组合实现不同的查询需求
     *
     * @param request 查询请求参数
     *                基础参数：
     *                - pageNum: 页码，默认1
     *                - pageSize: 每页大小，默认10
     *                - userId: 用户ID（精确查询）
     *                - activityType: 活动类型（精确查询单个类型）
     *                - activityCategory: 活动分类（分类查询该分类下所有类型）
     *                - startTime: 开始时间
     *                - endTime: 结束时间
     *                - ip: IP地址（模糊查询）
     *                
     *                使用示例：
     *                1. 查询详细日志：GET /api/admin/user-activity-logs?userId=123&pageNum=1&pageSize=20
     *                2. 查询所有认证相关活动：GET /api/admin/user-activity-logs?activityCategory=AUTHENTICATION
     *                3. 查询某用户登录失败：GET /api/admin/user-activity-logs?userId=123&activityType=LOGIN_FAILED
     *                4. 查询某IP认证相关活动：GET /api/admin/user-activity-logs?ip=192.168.1.1&activityCategory=AUTHENTICATION
     *                
     * @return 分页日志列表 IPage<UserActivityLogDTO>
     */
    @GetMapping
    public ApiResponse<IPage<UserActivityLogDTO>> getActivityLogs(@Valid @ModelAttribute QueryUserActivityLogRequest request) {
        IPage<UserActivityLogDTO> result = userActivityLogAppService.getActivityLogs(request);
        return ApiResponse.success(result);
    }
}