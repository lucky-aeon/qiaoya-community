package org.xhy.community.interfaces.codex.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.codex.dto.CodexConfigSetDTO;
import org.xhy.community.application.codex.dto.CodexInstanceDTO;
import org.xhy.community.application.codex.service.AdminCodexPersistentAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin/codex-p")
public class AdminCodexPersistentController {
    private final AdminCodexPersistentAppService appService;

    public AdminCodexPersistentController(AdminCodexPersistentAppService appService) {
        this.appService = appService;
    }

    @GetMapping("/configs")
    @RequiresPlanPermissions(items={@RequiresPlanPermissions.Item(code="CODEX_CONFIG_READ", name="Codex配置读取(持久化)")})
    public ApiResponse<CodexConfigSetDTO> getAll() {
        return ApiResponse.success(appService.getAll());
    }

    @GetMapping("/instances")
    @RequiresPlanPermissions(items={@RequiresPlanPermissions.Item(code="CODEX_CONFIG_READ", name="Codex配置读取(持久化)")})
    public ApiResponse<List<CodexInstanceDTO>> listInstances() {
        return ApiResponse.success(appService.listInstances());
    }

    @GetMapping("/instances/{id}")
    @RequiresPlanPermissions(items={@RequiresPlanPermissions.Item(code="CODEX_CONFIG_READ", name="Codex配置读取(持久化)")})
    public ApiResponse<CodexInstanceDTO> getInstance(@PathVariable String id) {
        return ApiResponse.success(appService.getInstance(id));
    }

    @PostMapping("/instances")
    @RequiresPlanPermissions(items={@RequiresPlanPermissions.Item(code="CODEX_CONFIG_WRITE", name="Codex配置写入(持久化)")})
    public ApiResponse<CodexInstanceDTO> create(@Valid @RequestBody CodexInstanceDTO req) {
        return ApiResponse.success("已创建", appService.createInstance(req));
    }

    @PutMapping("/instances/{id}")
    @RequiresPlanPermissions(items={@RequiresPlanPermissions.Item(code="CODEX_CONFIG_WRITE", name="Codex配置写入(持久化)")})
    public ApiResponse<CodexInstanceDTO> update(@PathVariable String id, @Valid @RequestBody CodexInstanceDTO req) {
        return ApiResponse.success("已保存", appService.updateInstance(id, req));
    }

    @DeleteMapping("/instances/{id}")
    @RequiresPlanPermissions(items={@RequiresPlanPermissions.Item(code="CODEX_CONFIG_WRITE", name="Codex配置写入(持久化)")})
    public ApiResponse<Void> delete(@PathVariable String id) {
        appService.deleteInstance(id);
        return ApiResponse.success("已删除", null);
    }

    // 说明：不再提供设置默认实例的接口；所有实例均可用，由调用方选择。
}
