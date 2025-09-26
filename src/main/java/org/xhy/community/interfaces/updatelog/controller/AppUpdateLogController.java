package org.xhy.community.interfaces.updatelog.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.updatelog.dto.UpdateLogDTO;
import org.xhy.community.application.updatelog.service.UpdateLogAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.List;

/**
 * 前台更新日志控制器
 * 提供面向前台用户的更新日志查询功能
 * @module 前台API
 */
@RestController
@RequestMapping("/api/app/update-logs")
public class AppUpdateLogController {

    private final UpdateLogAppService updateLogAppService;

    public AppUpdateLogController(UpdateLogAppService updateLogAppService) {
        this.updateLogAppService = updateLogAppService;
    }

    /**
     * 获取已发布的更新日志列表
     * 返回所有已发布状态的更新日志聚合根，按创建时间倒序排列
     * 包含完整的基本信息、作者名称和所有变更详情
     *
     * @return 已发布的更新日志完整列表
     */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "UPDATE_LOG_BROWSE", name = "查看更新日志")})
    public ApiResponse<List<UpdateLogDTO>> getPublishedUpdateLogs() {
        List<UpdateLogDTO> updateLogs = updateLogAppService.getPublishedUpdateLogs();
        return ApiResponse.success(updateLogs);
    }
}
