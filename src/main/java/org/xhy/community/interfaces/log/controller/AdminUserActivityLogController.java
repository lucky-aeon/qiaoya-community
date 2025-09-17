package org.xhy.community.interfaces.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.log.service.UserActivityLogAppService;
import org.xhy.community.application.user.dto.UserActivityLogDTO;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.log.request.QueryUserActivityLogRequest;

/**
 * 管理员用户活动日志控制器
 * 提供管理员查看用户活动日志的接口
 */
@RestController
@RequestMapping("/api/admin/user-activity-logs")
public class AdminUserActivityLogController {
    
    private final UserActivityLogAppService adminUserActivityLogAppService;
    
    public AdminUserActivityLogController(UserActivityLogAppService adminUserActivityLogAppService) {
        this.adminUserActivityLogAppService = adminUserActivityLogAppService;
    }
    
    /**
     * 分页查询用户活动日志
     * 管理员可以查看所有用户的活动日志，支持多条件筛选
     *
     * @param request 查询请求参数
     *                - pageNum: 页码，默认1
     *                - pageSize: 每页大小，默认10
     *                - email: 用户邮箱（模糊查询）
     *                - activityType: 活动类型
     *                - startTime: 开始时间
     *                - endTime: 结束时间
     *                - ip: IP地址（模糊查询）
     * @return 分页查询结果
     */
    @GetMapping
    public ApiResponse<IPage<UserActivityLogDTO>> getActivityLogs(@Valid @ModelAttribute QueryUserActivityLogRequest request) {
        try {
            IPage<UserActivityLogDTO> result = adminUserActivityLogAppService.getActivityLogs(request);
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取指定邮箱的登录失败统计
     * 用于安全分析，检测可能的暴力破解攻击
     *
     * @param email 邮箱地址
     * @param hours 统计时间范围（小时），默认24小时
     * @return 失败次数
     */
    @GetMapping("/login-failures/by-email")
    public ApiResponse<Long> getLoginFailuresByEmail(@RequestParam String email,
                                                    @RequestParam(defaultValue = "24") int hours) {
        try {
            Long count = adminUserActivityLogAppService.getLoginFailureCount(email, hours);
            return ApiResponse.success("查询成功", count);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取指定IP的登录失败统计
     * 用于安全分析，检测可能的恶意IP攻击
     *
     * @param ip IP地址
     * @param hours 统计时间范围（小时），默认24小时
     * @return 失败次数
     */
    @GetMapping("/login-failures/by-ip")
    public ApiResponse<Long> getLoginFailuresByIp(@RequestParam String ip,
                                                 @RequestParam(defaultValue = "24") int hours) {
        try {
            Long count = adminUserActivityLogAppService.getLoginFailureCountByIp(ip, hours);
            return ApiResponse.success("查询成功", count);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败：" + e.getMessage());
        }
    }
}