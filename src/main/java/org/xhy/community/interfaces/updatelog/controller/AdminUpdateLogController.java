package org.xhy.community.interfaces.updatelog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.updatelog.dto.UpdateLogDTO;
import org.xhy.community.application.updatelog.service.AdminUpdateLogAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.updatelog.request.CreateUpdateLogRequest;
import org.xhy.community.interfaces.updatelog.request.UpdateUpdateLogRequest;
import org.xhy.community.interfaces.updatelog.request.AdminUpdateLogQueryRequest;

import java.util.List;

/**
 * 管理员更新日志控制器
 * 提供更新日志聚合的管理功能，需要管理员权限
 * @module 更新日志管理
 */
@RestController
@RequestMapping("/api/admin/update-logs")
public class AdminUpdateLogController {

    private final AdminUpdateLogAppService adminUpdateLogAppService;

    public AdminUpdateLogController(AdminUpdateLogAppService adminUpdateLogAppService) {
        this.adminUpdateLogAppService = adminUpdateLogAppService;
    }

    /**
     * 创建更新日志
     * 创建包含变更详情的完整更新日志聚合
     *
     * @param request 创建请求，包含日志基本信息和变更详情列表
     * @return 创建成功的更新日志（含完整变更详情）
     */
    @PostMapping
    public ApiResponse<UpdateLogDTO> createUpdateLog(@Valid @RequestBody CreateUpdateLogRequest request) {
        String currentUserId = UserContext.getCurrentUserId();

        UpdateLogDTO updateLog = adminUpdateLogAppService.createUpdateLog(request, currentUserId);
        return ApiResponse.success(updateLog);
    }

    /**
     * 更新更新日志
     * 全量更新更新日志聚合，包括基本信息和变更详情
     *
     * @param id 更新日志ID
     * @param request 更新请求，包含完整的日志信息和变更详情
     * @return 更新后的更新日志（含完整变更详情）
     */
    @PutMapping("/{id}")
    public ApiResponse<UpdateLogDTO> updateUpdateLog(@PathVariable String id,
                                                   @Valid @RequestBody UpdateUpdateLogRequest request) {
        UpdateLogDTO updateLog = adminUpdateLogAppService.updateUpdateLog(id, request);
        return ApiResponse.success(updateLog);
    }

    /**
     * 获取更新日志详情
     * 获取包含完整变更详情的更新日志聚合
     *
     * @param id 更新日志ID
     * @return 更新日志详情（含完整变更详情）
     */
    @GetMapping("/{id}")
    public ApiResponse<UpdateLogDTO> getUpdateLog(@PathVariable String id) {
        UpdateLogDTO updateLog = adminUpdateLogAppService.getUpdateLogById(id);
        return ApiResponse.success(updateLog);
    }

    /**
     * 删除更新日志
     * 级联删除更新日志聚合及所有变更详情
     *
     * @param id 更新日志ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUpdateLog(@PathVariable String id) {
        adminUpdateLogAppService.deleteUpdateLog(id);
        return ApiResponse.success();
    }

    /**
     * 分页查询更新日志
     * 支持状态、版本号、标题条件筛选和分页
     *
     * @param request 查询请求参数
     * @return 更新日志分页列表（不含变更详情，提高查询性能）
     */
    @GetMapping
    public ApiResponse<IPage<UpdateLogDTO>> getUpdateLogs(AdminUpdateLogQueryRequest request) {
        IPage<UpdateLogDTO> updateLogs = adminUpdateLogAppService.getUpdateLogs(request);
        return ApiResponse.success(updateLogs);
    }

    /**
     * 切换更新日志状态
     * 在草稿和发布状态之间切换
     *
     * @param id 更新日志ID
     * @return 更新后的更新日志基本信息
     */
    @PutMapping("/{id}/toggle-status")
    public ApiResponse<UpdateLogDTO> toggleUpdateLogStatus(@PathVariable String id) {
        UpdateLogDTO updateLog = adminUpdateLogAppService.toggleUpdateLogStatus(id);
        return ApiResponse.success(updateLog);
    }
}