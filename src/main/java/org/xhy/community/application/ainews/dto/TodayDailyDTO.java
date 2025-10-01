package org.xhy.community.application.ainews.dto;

import java.util.List;

/**
 * 今日 AI 日报汇总返回
 * - date: 当天日期（或最新一期日期）
 * - titles: 当天所有日报标题列表（不含详情）
 */
public class TodayDailyDTO {
    private String date;
    private List<String> titles;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public List<String> getTitles() { return titles; }
    public void setTitles(List<String> titles) { this.titles = titles; }
}
