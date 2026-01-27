package org.xhy.community.interfaces.codex.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.codex.dto.CodexPublicInfoDTO;
import org.xhy.community.application.codex.dto.CodexPublicInstanceDTO;
import org.xhy.community.application.codex.service.CodexPersistentAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/app/codex-p")
public class CodexPersistentController {
    private final CodexPersistentAppService appService;

    public CodexPersistentController(CodexPersistentAppService appService) {
        this.appService = appService;
    }

    /** 默认实例信息（兼容单实例语义） */
    @GetMapping("/info")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CODEX_INFO_READ", name = "Codex信息读取(持久化)")})
    public ApiResponse<CodexPublicInfoDTO> getInfo() {
        return ApiResponse.success(appService.getDefaultPublicInfo());
    }

    /** 所有启用实例的公共信息列表 */
    @GetMapping("/infos")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CODEX_INFO_READ", name = "Codex信息读取(持久化)")})
    public ApiResponse<List<CodexPublicInstanceDTO>> listInfos() {
        return ApiResponse.success(appService.listPublicInfos());
    }
}

