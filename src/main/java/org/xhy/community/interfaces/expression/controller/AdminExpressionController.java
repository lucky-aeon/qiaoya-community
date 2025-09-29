package org.xhy.community.interfaces.expression.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.expression.dto.ExpressionDTO;
import org.xhy.community.application.expression.service.AdminExpressionAppService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.expression.request.CreateExpressionRequest;
import org.xhy.community.interfaces.expression.request.ExpressionQueryRequest;
import org.xhy.community.interfaces.expression.request.UpdateExpressionRequest;

@RestController
@RequestMapping("/api/admin/expressions")
public class AdminExpressionController {

    private final AdminExpressionAppService adminExpressionAppService;

    public AdminExpressionController(AdminExpressionAppService adminExpressionAppService) {
        this.adminExpressionAppService = adminExpressionAppService;
    }

    /** 创建表情类型 */
    @PostMapping
    @ActivityLog(ActivityType.ADMIN_EXPRESSION_CREATE)
    public ApiResponse<ExpressionDTO> create(@Valid @RequestBody CreateExpressionRequest request) {
        ExpressionDTO dto = adminExpressionAppService.create(request);
        return ApiResponse.success(dto);
    }

    /** 更新表情类型 */
    @PutMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_EXPRESSION_UPDATE)
    public ApiResponse<ExpressionDTO> update(@PathVariable String id,
                                             @Valid @RequestBody UpdateExpressionRequest request) {
        ExpressionDTO dto = adminExpressionAppService.update(id, request);
        return ApiResponse.success(dto);
    }

    /** 删除表情类型（在用校验） */
    @DeleteMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_EXPRESSION_DELETE)
    public ApiResponse<Void> delete(@PathVariable String id) {
        adminExpressionAppService.delete(id);
        return ApiResponse.success();
    }

    /** 启停切换 */
    @PutMapping("/{id}/toggle")
    @ActivityLog(ActivityType.ADMIN_EXPRESSION_TOGGLE)
    public ApiResponse<Boolean> toggle(@PathVariable String id) {
        boolean isActive = adminExpressionAppService.toggle(id);
        return ApiResponse.success(isActive);
    }

    /** 分页查询 */
    @GetMapping
    public ApiResponse<IPage<ExpressionDTO>> page(ExpressionQueryRequest request) {
        IPage<ExpressionDTO> page = adminExpressionAppService.page(request);
        return ApiResponse.success(page);
    }
}

