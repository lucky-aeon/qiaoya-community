package org.xhy.community.application.codex.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.codex.assembler.CodexAssembler;
import org.xhy.community.application.codex.dto.CodexConfigDTO;
import org.xhy.community.domain.codex.service.CodexConfigDomainService;
import org.xhy.community.domain.codex.valueobject.CodexConfig;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AdminCodexAppService {
    private final CodexConfigDomainService configService;

    public AdminCodexAppService(CodexConfigDomainService configService) {
        this.configService = configService;
    }

    public CodexConfigDTO getConfig() {
        CodexConfig cfg = configService.getConfig();
        return CodexAssembler.toDTO(cfg);
    }

    public CodexConfigDTO updateConfig(CodexConfigDTO req) {
        CodexConfig cfg = CodexAssembler.fromDTO(req);
        if (cfg.getEnabled() == null) cfg.setEnabled(Boolean.TRUE);
        // 统一时间格式：yyyy-MM-dd'T'HH:mm:ssXXX，例如 2026-01-11T15:21:09+08:00（去除多余小数秒）
        String now = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        cfg.setLastUpdatedAt(now);
        configService.updateConfig(cfg);
        return CodexAssembler.toDTO(cfg);
    }
}
