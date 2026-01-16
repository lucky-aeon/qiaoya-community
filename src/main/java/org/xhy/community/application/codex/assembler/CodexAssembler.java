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
            dto.setWeeklyWindowStart(formatTs(info.weeklyWindowStart));
            dto.setWeeklyWindowEnd(formatTs(info.weeklyWindowEnd));
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

    // 统一时间格式 -> yyyy-MM-dd HH:mm:ss（去掉 T 与时区）
    private static String formatTs(String ts) {
        if (ts == null || ts.isBlank()) return null;
        try {
            // 优先按 OffsetDateTime 解析
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(ts);
            return odt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(ts);
                return ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ignore) {
                return ts;
            }
        }
    }
}
