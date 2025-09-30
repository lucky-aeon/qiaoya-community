package org.xhy.community.application.ainews.dto;

public class HistoryDateDTO {
    private String date; // yyyy-MM-dd
    private Integer count;

    public HistoryDateDTO() {}
    public HistoryDateDTO(String date, Integer count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

