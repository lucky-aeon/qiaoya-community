package org.xhy.community.interfaces.codex.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.codex.dto.CodexPublicInfoDTO;
import org.xhy.community.application.codex.service.CodexAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;

@RestController
@RequestMapping("/api/app/codex")
public class CodexController {
    private final CodexAppService codexAppService;

    public CodexController(CodexAppService codexAppService) {
        this.codexAppService = codexAppService;
    }

    @GetMapping("/info")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CODEX_INFO_READ", name = "Codex信息读取")})
    public ApiResponse<CodexPublicInfoDTO> getInfo() {
        CodexPublicInfoDTO info = codexAppService.getPublicInfo();
        return ApiResponse.success(info);
    }
}

