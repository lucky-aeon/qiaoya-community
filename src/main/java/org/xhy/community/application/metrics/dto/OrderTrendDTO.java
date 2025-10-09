package org.xhy.community.application.metrics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单趋势DTO
 */
public class OrderTrendDTO {

    /** 订单数量趋势 */
    private List<TrendDataPointDTO> countTrend;

    /** 订单金额趋势 */
    private List<AmountTrendDataPointDTO> amountTrend;

    public OrderTrendDTO() {
    }

    public OrderTrendDTO(List<TrendDataPointDTO> countTrend, List<AmountTrendDataPointDTO> amountTrend) {
        this.countTrend = countTrend;
        this.amountTrend = amountTrend;
    }

    // Getters and Setters
    public List<TrendDataPointDTO> getCountTrend() { return countTrend; }
    public void setCountTrend(List<TrendDataPointDTO> countTrend) { this.countTrend = countTrend; }

    public List<AmountTrendDataPointDTO> getAmountTrend() { return amountTrend; }
    public void setAmountTrend(List<AmountTrendDataPointDTO> amountTrend) { this.amountTrend = amountTrend; }

    /**
     * 金额趋势数据点（用于订单金额统计）
     */
    public static class AmountTrendDataPointDTO {
        private LocalDate date;
        private BigDecimal amount;

        public AmountTrendDataPointDTO() {
        }

        public AmountTrendDataPointDTO(LocalDate date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
