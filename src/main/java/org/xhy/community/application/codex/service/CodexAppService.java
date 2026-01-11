package org.xhy.community.application.codex.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.codex.assembler.CodexAssembler;
import org.xhy.community.application.codex.dto.CodexPublicInfoDTO;
import org.xhy.community.domain.codex.service.CodexConfigDomainService;
import org.xhy.community.domain.codex.valueobject.CodexConfig;
import org.xhy.community.infrastructure.codex.CodexHttpClient;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CodexErrorCode;

@Service
public class CodexAppService {
    private final CodexConfigDomainService configService;
    private final CodexHttpClient codexHttpClient;

    public CodexAppService(CodexConfigDomainService configService, CodexHttpClient codexHttpClient) {
        this.configService = configService;
        this.codexHttpClient = codexHttpClient;
    }

    public CodexPublicInfoDTO getPublicInfo() {
        CodexConfig cfg = configService.getConfig();
        if (cfg == null) {
            throw new BusinessException(CodexErrorCode.CODEX_CONFIG_NOT_FOUND);
        }
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            throw new BusinessException(CodexErrorCode.CODEX_DISABLED);
        }
        // 容错：authorization 为空视为授权失败（前端可据错误码提示重新配置）
        if (cfg.getAuthorization() == null || cfg.getAuthorization().isBlank()) {
            throw new BusinessException(CodexErrorCode.CODEX_UNAUTHORIZED);
        }
        CodexHttpClient.UserInfoResponse info = codexHttpClient.fetchUserInfo(
                cfg.getBaseUrl(), cfg.getAuthorization(), cfg.getCookieToken());
        // 容错：若上游返回体无法解析或关键用量字段为空，返回明确错误码，避免前端拿到 null
        if (info == null) {
            throw new BusinessException(CodexErrorCode.CODEX_FETCH_FAILED);
        }
        boolean allUsageNull = isBlank(info.weeklySpentUsd)
                && isBlank(info.weeklyBudgetUsd)
                && isBlank(info.dailySpentUsd)
                && isBlank(info.dailyBudgetUsd);
        if (allUsageNull) {
            // 极端情况：非401/403但返回空数据，视为拉取失败（可能 token 过期或服务异常）
            throw new BusinessException(CodexErrorCode.CODEX_FETCH_FAILED);
        }
        return CodexAssembler.toPublicDTO(cfg.getApiKey(), cfg.getUsageDocUrl(), info);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
