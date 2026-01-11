package org.xhy.community.infrastructure.codex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CodexErrorCode;

import java.util.Map;

@Component
public class CodexHttpClient {
    private static final Logger log = LoggerFactory.getLogger(CodexHttpClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfoResponse {
        @JsonProperty("user_id") public String userId;
        public String username;
        public String email;
        @JsonProperty("user_type") public String userType;
        @JsonProperty("balance_usd") public String balanceUsd;
        @JsonProperty("total_spent_usd") public String totalSpentUsd;
        @JsonProperty("api_key") public String apiKey;
        @JsonProperty("plan_type") public String planType;
        @JsonProperty("plan_expires_at") public String planExpiresAt;
        @JsonProperty("monthly_budget_usd") public String monthlyBudgetUsd;
        @JsonProperty("daily_budget_usd") public String dailyBudgetUsd;
        @JsonProperty("daily_spent_usd") public String dailySpentUsd;
        @JsonProperty("monthly_spent_usd") public String monthlySpentUsd;
        @JsonProperty("weekly_spent_usd") public String weeklySpentUsd;
        @JsonProperty("weekly_budget_usd") public String weeklyBudgetUsd;
        @JsonProperty("weekly_window_start") public String weeklyWindowStart;
        @JsonProperty("weekly_window_end") public String weeklyWindowEnd;
        @JsonProperty("total_quota") public Long totalQuota;
        @JsonProperty("used_quota") public Long usedQuota;
        @JsonProperty("remaining_quota") public Long remainingQuota;
    }

    public UserInfoResponse fetchUserInfo(String baseUrl, String authorization, String cookieToken) {
        String url = buildEndpoint(baseUrl);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer "+authorization);
            headers.set("User-Agent", "qiaoya-community/1.0");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            if (cookieToken != null && !cookieToken.isBlank()) {
                headers.add(HttpHeaders.COOKIE, "token=" + cookieToken);
            }
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("[Codex] 非2xx响应: status={} body={}", resp.getStatusCode(), resp.getBody());
                throw new BusinessException(CodexErrorCode.CODEX_FETCH_FAILED);
            }
            // 直接映射到 UserInfoResponse，保留未知字段的容错
            return objectMapper.readValue(resp.getBody(), UserInfoResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new BusinessException(CodexErrorCode.CODEX_UNAUTHORIZED);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new BusinessException(CodexErrorCode.CODEX_UNAUTHORIZED);
        } catch (Exception e) {
            log.warn("[Codex] 拉取用户信息失败", e.getMessage());
            throw new BusinessException(CodexErrorCode.CODEX_FETCH_FAILED);
        }
    }

    private String buildEndpoint(String baseUrl) {
        final String defaultUrl = "https://codex.packycode.com/api/backend/users/info";
        if (baseUrl == null) return defaultUrl;
        String v = baseUrl.trim();
        if (v.isEmpty()) return defaultUrl;
        // 如果已经是完整的 /api/backend/users/info 端点，就直接用
        if (v.contains("/api/backend/users/info")) {
            // 去掉多余的重复片段（防御性）
            while (v.endsWith("/api/backend/users/info/api/backend/users/info")) {
                v = v.substring(0, v.length() - "/api/backend/users/info".length());
            }
            return v;
        }
        // 否则认为是基础域名，拼接标准路径
        v = v.replaceAll("/+$", "");
        return v + "/api/backend/users/info";
    }
}
