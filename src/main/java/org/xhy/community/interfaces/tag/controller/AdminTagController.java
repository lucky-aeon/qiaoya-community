package org.xhy.community.interfaces.tag.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.tag.dto.TagDefinitionDTO;
import org.xhy.community.application.tag.service.AdminTagAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

@RestController
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminTagAppService adminTagAppService;

    public AdminTagController(AdminTagAppService adminTagAppService) {
        this.adminTagAppService = adminTagAppService;
    }

    @PostMapping
    public ApiResponse<TagDefinitionDTO> createTag(@Valid @RequestBody org.xhy.community.interfaces.tag.request.CreateTagRequest req) {
        return ApiResponse.success("创建成功",adminTagAppService.createTag(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<TagDefinitionDTO> updateTag(@PathVariable String id,
                                                   @Valid @RequestBody org.xhy.community.interfaces.tag.request.UpdateTagRequest req) {
        return ApiResponse.success("修改成功",adminTagAppService.updateTag(id, req));
    }

    @GetMapping
    public ApiResponse<IPage<TagDefinitionDTO>> listTags(@Valid org.xhy.community.interfaces.tag.request.TagQueryRequest req) {
        return ApiResponse.success(adminTagAppService.listTags(req));
    }

    @PostMapping("/{id}/scopes")
    public ApiResponse<Void> addScope(@PathVariable String id,
                                      @Valid @RequestBody org.xhy.community.interfaces.tag.request.AddScopeRequest req) {
        adminTagAppService.addScope(id, req);
        return ApiResponse.success("添加成功");
    }

    @GetMapping("/{id}/scopes")
    public ApiResponse<java.util.List<org.xhy.community.application.tag.dto.TagScopeDTO>> listScopes(@PathVariable String id) {
        return ApiResponse.success(adminTagAppService.listScopes(id));
    }

    @DeleteMapping("/scopes/{scopeId}")
    public ApiResponse<Void> removeScope(@PathVariable String scopeId) {
        adminTagAppService.removeScope(scopeId);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/assign")
    public ApiResponse<Void> assign(@Valid @RequestBody org.xhy.community.interfaces.tag.request.ManualAssignRequest req) {
        adminTagAppService.assignTagToUser(req);
        return ApiResponse.success("分配成功");
    }

    @PostMapping("/revoke")
    public ApiResponse<Void> revoke(@Valid @RequestBody org.xhy.community.interfaces.tag.request.ManualRevokeRequest req) {
        adminTagAppService.revokeUserTag(req);
        return ApiResponse.success("移除成功");
    }
}
