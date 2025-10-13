package org.xhy.community.interfaces.tag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.tag.dto.TagDefinitionDTO;
import org.xhy.community.application.tag.dto.TagScopeDTO;
import org.xhy.community.application.tag.service.AdminTagAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.interfaces.tag.request.CreateTagRequest;
import org.xhy.community.interfaces.tag.request.UpdateTagRequest;
import org.xhy.community.interfaces.tag.request.TagQueryRequest;
import org.xhy.community.interfaces.tag.request.AddScopeRequest;
import org.xhy.community.interfaces.tag.request.ManualAssignRequest;
import org.xhy.community.interfaces.tag.request.ManualRevokeRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminTagAppService adminTagAppService;

    public AdminTagController(AdminTagAppService adminTagAppService) {
        this.adminTagAppService = adminTagAppService;
    }

    @PostMapping
    @ActivityLog(ActivityType.ADMIN_TAG_CREATE)
    public ApiResponse<TagDefinitionDTO> createTag(@Valid @RequestBody CreateTagRequest req) {
        return ApiResponse.success("创建成功",adminTagAppService.createTag(req));
    }

    @PutMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_TAG_UPDATE)
    public ApiResponse<TagDefinitionDTO> updateTag(@PathVariable String id,
                                                   @Valid @RequestBody UpdateTagRequest req) {
        return ApiResponse.success("修改成功",adminTagAppService.updateTag(id, req));
    }

    @GetMapping
    public ApiResponse<IPage<TagDefinitionDTO>> listTags(@Valid TagQueryRequest req) {
        return ApiResponse.success(adminTagAppService.listTags(req));
    }

    @PostMapping("/{id}/scopes")
    @ActivityLog(ActivityType.ADMIN_TAG_ADD_SCOPE)
    public ApiResponse<Void> addScope(@PathVariable String id,
                                      @Valid @RequestBody AddScopeRequest req) {
        adminTagAppService.addScope(id, req);
        return ApiResponse.success("添加成功");
    }

    @GetMapping("/{id}/scopes")
    public ApiResponse<List<TagScopeDTO>> listScopes(@PathVariable String id) {
        return ApiResponse.success(adminTagAppService.listScopes(id));
    }

    @DeleteMapping("/scopes/{scopeId}")
    @ActivityLog(ActivityType.ADMIN_TAG_REMOVE_SCOPE)
    public ApiResponse<Void> removeScope(@PathVariable String scopeId) {
        adminTagAppService.removeScope(scopeId);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/assign")
    @ActivityLog(ActivityType.ADMIN_TAG_ASSIGN)
    public ApiResponse<Void> assign(@Valid @RequestBody ManualAssignRequest req) {
        adminTagAppService.assignTagToUser(req);
        return ApiResponse.success("分配成功");
    }

    @PostMapping("/revoke")
    @ActivityLog(ActivityType.ADMIN_TAG_REVOKE)
    public ApiResponse<Void> revoke(@Valid @RequestBody ManualRevokeRequest req) {
        adminTagAppService.revokeUserTag(req);
        return ApiResponse.success("移除成功");
    }
}
