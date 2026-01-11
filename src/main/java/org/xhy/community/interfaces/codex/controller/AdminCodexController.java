package org.xhy.community.interfaces.codex.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.codex.dto.CodexConfigDTO;
import org.xhy.community.application.codex.service.AdminCodexAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;

@RestController
@RequestMapping("/api/admin/codex")
public class AdminCodexController {
    private final AdminCodexAppService adminCodexAppService;

    public AdminCodexController(AdminCodexAppService adminCodexAppService) {
        this.adminCodexAppService = adminCodexAppService;
    }

    @GetMapping("/config")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CODEX_CONFIG_READ", name = "Codex配置读取")})
    public ApiResponse<CodexConfigDTO> getConfig() {
        return ApiResponse.success(adminCodexAppService.getConfig());
    }

    @PutMapping("/config")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CODEX_CONFIG_WRITE", name = "Codex配置写入")})
    public ApiResponse<CodexConfigDTO> updateConfig(@Valid @RequestBody CodexConfigDTO request) {
        CodexConfigDTO dto = adminCodexAppService.updateConfig(request);
        return ApiResponse.success("已保存", dto);
    }
}

