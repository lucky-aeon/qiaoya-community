package org.xhy.community.application.codex.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.codex.dto.CodexPublicInfoDTO;
import org.xhy.community.application.codex.dto.CodexPublicInstanceDTO;
import org.xhy.community.domain.codex.valueobject.CodexConfigSet;
import org.xhy.community.domain.codex.valueobject.CodexInstance;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.codex.CodexHttpClient;
import org.xhy.community.infrastructure.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodexPersistentAppService {
    private final SystemConfigDomainService systemConfigDomainService;
    private final CodexHttpClient httpClient;

    public CodexPersistentAppService(SystemConfigDomainService systemConfigDomainService,
                                     CodexHttpClient httpClient) {
        this.systemConfigDomainService = systemConfigDomainService;
        this.httpClient = httpClient;
    }

    // 不再有“默认”概念，保留接口但改为：若存在至少一个启用实例，返回第一个启用实例的信息；否则返回空
    public CodexPublicInfoDTO getDefaultPublicInfo() {
        CodexConfigSet set = loadSet();
        for (CodexInstance ins : set.getInstances()) {
            if (!Boolean.FALSE.equals(ins.getEnabled()) && !isBlank(ins.getApiKey())) {
                return buildPublicDTO(ins);
            }
        }
        return null;
    }

    public List<CodexPublicInstanceDTO> listPublicInfos() {
        CodexConfigSet set = loadSet();
        List<CodexPublicInstanceDTO> out = new ArrayList<>();
        for (CodexInstance ins : set.getInstances()) {
            if (Boolean.FALSE.equals(ins.getEnabled())) continue;
            if (isBlank(ins.getApiKey())) continue;
            CodexPublicInstanceDTO dto = new CodexPublicInstanceDTO();
            dto.setId(ins.getId());
            dto.setName(ins.getName());
            CodexPublicInfoDTO base = buildPublicDTO(ins);
            dto.setApiKey(base.getApiKey());
            dto.setWeeklySpentUsd(base.getWeeklySpentUsd());
            dto.setWeeklyBudgetUsd(base.getWeeklyBudgetUsd());
            dto.setDailySpentUsd(base.getDailySpentUsd());
            dto.setDailyBudgetUsd(base.getDailyBudgetUsd());
            dto.setUsageDocUrl(base.getUsageDocUrl());
            dto.setUsageFetchFailed(base.getUsageFetchFailed());
            dto.setWeeklyWindowStart(base.getWeeklyWindowStart());
            dto.setWeeklyWindowEnd(base.getWeeklyWindowEnd());
            out.add(dto);
        }
        return out;
    }

    // helpers
    private CodexConfigSet loadSet() {
        CodexConfigSet set = systemConfigDomainService.getConfigData(SystemConfigType.CODEX_CONFIGS, CodexConfigSet.class);
        if (set == null) set = new CodexConfigSet();
        if (set.getInstances() == null) set.setInstances(new java.util.ArrayList<>());
        return set;
    }
    // 默认逻辑已去除

    private CodexPublicInfoDTO buildPublicDTO(CodexInstance ins) {
        if (ins.getAuthorization() == null || ins.getAuthorization().isBlank()) {
            CodexPublicInfoDTO dto = org.xhy.community.application.codex.assembler.CodexAssembler
                    .toPublicDTO(ins.getApiKey(), ins.getUsageDocUrl(), null);
            dto.setUsageFetchFailed(true);
            return dto;
        }
        try {
            CodexHttpClient.UserInfoResponse info = httpClient.fetchUserInfo(
                    ins.getBaseUrl(), ins.getAuthorization(), ins.getCookieToken());
            if (info == null) {
                CodexPublicInfoDTO dto = org.xhy.community.application.codex.assembler.CodexAssembler
                        .toPublicDTO(ins.getApiKey(), ins.getUsageDocUrl(), null);
                dto.setUsageFetchFailed(true);
                return dto;
            }
            boolean allNull = isBlank(info.weeklySpentUsd) && isBlank(info.weeklyBudgetUsd)
                    && isBlank(info.dailySpentUsd) && isBlank(info.dailyBudgetUsd);
            CodexPublicInfoDTO dto = org.xhy.community.application.codex.assembler.CodexAssembler
                    .toPublicDTO(ins.getApiKey(), ins.getUsageDocUrl(), info);
            dto.setUsageFetchFailed(allNull);
            return dto;
        } catch (BusinessException ex) {
            CodexPublicInfoDTO fallback = org.xhy.community.application.codex.assembler.CodexAssembler
                    .toPublicDTO(ins.getApiKey(), ins.getUsageDocUrl(), null);
            fallback.setUsageFetchFailed(true);
            return fallback;
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
