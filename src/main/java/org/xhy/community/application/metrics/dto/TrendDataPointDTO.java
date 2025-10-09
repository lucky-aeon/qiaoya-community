package org.xhy.community.application.metrics.dto;

import java.time.LocalDate;

/**
 * 折线图数据点DTO
 * 用于统计趋势图的单个数据点
 */
public class TrendDataPointDTO {

    /** 日期 */
    private LocalDate date;

    /** 数值 */
    private Long value;

    /** 标签（可选，用于区分不同套餐等） */
    private String label;

    public TrendDataPointDTO() {
    }

    public TrendDataPointDTO(LocalDate date, Long value) {
        this.date = date;
        this.value = value;
    }

    public TrendDataPointDTO(LocalDate date, Long value, String label) {
        this.date = date;
        this.value = value;
        this.label = label;
    }

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
