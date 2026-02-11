package org.xhy.community.interfaces.updatelog.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
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
@Validated
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

    /**
     * 根据ID获取已发布的更新日志详情
     *
     * @param updateLogId 更新日志ID
     * @return 更新日志详情
     */
    @GetMapping("/{updateLogId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "UPDATE_LOG_BROWSE", name = "查看更新日志")})
    public ApiResponse<UpdateLogDTO> getPublishedUpdateLogDetail(
            @PathVariable @NotBlank(message = "更新日志ID不能为空") String updateLogId) {
        UpdateLogDTO updateLog = updateLogAppService.getPublishedUpdateLogDetail(updateLogId);
        return ApiResponse.success(updateLog);
    }
}
