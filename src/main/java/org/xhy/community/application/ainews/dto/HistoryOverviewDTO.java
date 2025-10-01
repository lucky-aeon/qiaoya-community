package org.xhy.community.application.ainews.dto;

/**
 * 往期概览 DTO（用于分页展示）
 * - title: 大标题（例如可用日期或“AI日报 yyyy-MM-dd”）
 * - date: 日期（yyyy-MM-dd）
 * - count: 当日 AI 日报数量
 */
public class HistoryOverviewDTO {
    private String title;
    private String date;
    private Integer count;

    public HistoryOverviewDTO() {}
    public HistoryOverviewDTO(String title, String date, Integer count) {
        this.title = title;
        this.date = date;
        this.count = count;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

