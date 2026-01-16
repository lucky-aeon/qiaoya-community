package org.xhy.community.application.codex.dto;

public class CodexPublicInfoDTO {
    private String apiKey;
    private String weeklySpentUsd;
    private String weeklyBudgetUsd;
    private String dailySpentUsd;
    private String dailyBudgetUsd;
    private String usageDocUrl;
    // 用量拉取失败容错标记（仅用于前端兜底处理）
    private Boolean usageFetchFailed;
    private String weeklyWindowStart;
    private String weeklyWindowEnd;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getWeeklySpentUsd() { return weeklySpentUsd; }
    public void setWeeklySpentUsd(String weeklySpentUsd) { this.weeklySpentUsd = weeklySpentUsd; }
    public String getWeeklyBudgetUsd() { return weeklyBudgetUsd; }
    public void setWeeklyBudgetUsd(String weeklyBudgetUsd) { this.weeklyBudgetUsd = weeklyBudgetUsd; }
    public String getDailySpentUsd() { return dailySpentUsd; }
    public void setDailySpentUsd(String dailySpentUsd) { this.dailySpentUsd = dailySpentUsd; }
    public String getDailyBudgetUsd() { return dailyBudgetUsd; }
    public void setDailyBudgetUsd(String dailyBudgetUsd) { this.dailyBudgetUsd = dailyBudgetUsd; }
    public String getUsageDocUrl() { return usageDocUrl; }
    public void setUsageDocUrl(String usageDocUrl) { this.usageDocUrl = usageDocUrl; }
    public Boolean getUsageFetchFailed() { return usageFetchFailed; }
    public void setUsageFetchFailed(Boolean usageFetchFailed) { this.usageFetchFailed = usageFetchFailed; }
    public String getWeeklyWindowStart() { return weeklyWindowStart; }
    public void setWeeklyWindowStart(String weeklyWindowStart) { this.weeklyWindowStart = weeklyWindowStart; }
    public String getWeeklyWindowEnd() { return weeklyWindowEnd; }
    public void setWeeklyWindowEnd(String weeklyWindowEnd) { this.weeklyWindowEnd = weeklyWindowEnd; }
}
