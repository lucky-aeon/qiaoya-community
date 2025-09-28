package org.xhy.community.interfaces.cdk.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.cdk.dto.CDKDTO;
import org.xhy.community.application.cdk.service.AdminCDKAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.cdk.request.CreateCDKRequest;
import org.xhy.community.interfaces.cdk.request.CDKQueryRequest;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

import java.util.List;

/**
 * 管理员CDK管理控制器
 * 提供CDK的创建、查询、删除等管理功能，需要管理员权限
 * @module CDK管理
 */
@RestController
@RequestMapping("/api/admin/cdk")
public class AdminCDKController {
    
    private final AdminCDKAppService adminCDKAppService;
    
    public AdminCDKController(AdminCDKAppService adminCDKAppService) {
        this.adminCDKAppService = adminCDKAppService;
    }
    
    /**
     * 创建CDK
     * 支持批量生成CDK，可绑定套餐或课程
     * @param request 创建CDK请求参数
     * @return 创建成功的CDK列表
     */
    @PostMapping
    @ActivityLog(ActivityType.ADMIN_CDK_CREATE)
    public ApiResponse<List<CDKDTO>> createCDK(@Valid @RequestBody CreateCDKRequest request) {
        List<CDKDTO> cdkList = adminCDKAppService.createCDK(request);
        return ApiResponse.success("创建成功",cdkList);
    }
    
    /**
     * 分页获取CDK列表
     * 支持按类型、目标ID、状态等条件筛选
     * @param request 查询请求参数
     * @return 分页CDK列表
     */
    @GetMapping
    public ApiResponse<IPage<CDKDTO>> getCDKs(CDKQueryRequest request) {
        IPage<CDKDTO> cdks = adminCDKAppService.getPagedCDKs(request);
        return ApiResponse.success(cdks);
    }
    
    /**
     * 删除CDK
     * 删除指定的CDK（软删除），已使用的CDK不可删除
     * @param id CDK的ID
     * @return 空响应
     */
    @DeleteMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_CDK_DELETE)
    public ApiResponse<Void> deleteCDK(@PathVariable String id) {
        adminCDKAppService.deleteCDK(id);
        return ApiResponse.success("删除成功");
    }
}
