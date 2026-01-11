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
        // 容错：authorization 为空也返回 apiKey，标记 usageFetchFailed
        if (cfg.getAuthorization() == null || cfg.getAuthorization().isBlank()) {
            CodexPublicInfoDTO dto = CodexAssembler.toPublicDTO(cfg.getApiKey(), cfg.getUsageDocUrl(), null);
            dto.setUsageFetchFailed(true);
            return dto;
        }
        CodexPublicInfoDTO dto;
        try {
            CodexHttpClient.UserInfoResponse info = codexHttpClient.fetchUserInfo(
                    cfg.getBaseUrl(), cfg.getAuthorization(), cfg.getCookieToken());
            if (info == null) {
                dto = CodexAssembler.toPublicDTO(cfg.getApiKey(), cfg.getUsageDocUrl(), null);
                dto.setUsageFetchFailed(true);
                return dto;
            }
            boolean allUsageNull = isBlank(info.weeklySpentUsd)
                    && isBlank(info.weeklyBudgetUsd)
                    && isBlank(info.dailySpentUsd)
                    && isBlank(info.dailyBudgetUsd);
            dto = CodexAssembler.toPublicDTO(cfg.getApiKey(), cfg.getUsageDocUrl(), info);
            if (allUsageNull) {
                dto.setUsageFetchFailed(true);
            } else {
                dto.setUsageFetchFailed(false);
            }
            return dto;
        } catch (BusinessException ex) {
            // 只要是用量拉取失败（含授权失败），也返回 apiKey，并在 DTO 上标记兜底
            CodexPublicInfoDTO fallback = CodexAssembler.toPublicDTO(cfg.getApiKey(), cfg.getUsageDocUrl(), null);
            fallback.setUsageFetchFailed(true);
            return fallback;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
