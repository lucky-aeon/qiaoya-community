package org.xhy.community.application.codex.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.codex.dto.CodexConfigDTO;
import org.xhy.community.application.codex.dto.CodexPublicInfoDTO;
import org.xhy.community.domain.codex.valueobject.CodexConfig;
import org.xhy.community.infrastructure.codex.CodexHttpClient;

public class CodexAssembler {

    private CodexAssembler() {}

    public static CodexConfigDTO toDTO(CodexConfig cfg) {
        if (cfg == null) return null;
        CodexConfigDTO dto = new CodexConfigDTO();
        BeanUtils.copyProperties(cfg, dto);
        return dto;
    }

    public static CodexConfig fromDTO(CodexConfigDTO dto) {
        if (dto == null) return null;
        CodexConfig cfg = new CodexConfig();
        BeanUtils.copyProperties(dto, cfg);
        return cfg;
    }

    public static CodexPublicInfoDTO toPublicDTO(String apiKey, String usageDocUrl, CodexHttpClient.UserInfoResponse info) {
        CodexPublicInfoDTO dto = new CodexPublicInfoDTO();
        dto.setApiKey(apiKey);
        dto.setUsageDocUrl(usageDocUrl);
        if (info != null) {
            dto.setWeeklySpentUsd(format2(info.weeklySpentUsd));
            dto.setWeeklyBudgetUsd(format2(info.weeklyBudgetUsd));
            dto.setDailySpentUsd(format2(info.dailySpentUsd));
            dto.setDailyBudgetUsd(format2(info.dailyBudgetUsd));
        }
        return dto;
    }

    // 规范为两位小数；输入可能为 null 或非数值字符串
    private static String format2(String v) {
        if (v == null || v.isBlank()) return "0.00";
        try {
            java.math.BigDecimal bd = new java.math.BigDecimal(v);
            bd = bd.setScale(2, java.math.RoundingMode.HALF_UP);
            return bd.toPlainString();
        } catch (Exception e) {
            return v; // 保底直接回传原值
        }
    }
}
