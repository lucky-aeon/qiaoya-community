package org.xhy.community.application.metrics.assembler;

import org.xhy.community.application.metrics.dto.*;
import org.xhy.community.application.metrics.dto.OrderTrendDTO.AmountTrendDataPointDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计指标转换器
 */
public class MetricsAssembler {

    /**
     * 将活跃用户Map数据转换为DTO
     */
    public static ActiveUserTrendDTO toActiveUserTrendDTO(
            Map<LocalDate, Long> totalTrend,
            Map<String, Map<LocalDate, Long>> subscriptionTrends) {

        List<TrendDataPointDTO> totalTrendList = toTrendDataPoints(totalTrend);

        Map<String, List<TrendDataPointDTO>> subscriptionTrendMap = subscriptionTrends.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toTrendDataPoints(entry.getValue())
            ));

        return new ActiveUserTrendDTO(totalTrendList, subscriptionTrendMap);
    }

    /**
     * 将订单Map数据转换为DTO
     */
    public static OrderTrendDTO toOrderTrendDTO(
            Map<LocalDate, Long> countTrend,
            Map<LocalDate, BigDecimal> amountTrend) {

        List<TrendDataPointDTO> countTrendList = toTrendDataPoints(countTrend);

        List<AmountTrendDataPointDTO> amountTrendList = amountTrend.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new AmountTrendDataPointDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        return new OrderTrendDTO(countTrendList, amountTrendList);
    }

    /**
     * 将注册用户Map数据转换为DTO
     */
    public static RegistrationTrendDTO toRegistrationTrendDTO(Map<LocalDate, Long> trend) {
        List<TrendDataPointDTO> trendList = toTrendDataPoints(trend);
        return new RegistrationTrendDTO(trendList);
    }

    /**
     * 将课程Map数据转换为DTO
     */
    public static CourseTrendDTO toCourseTrendDTO(Map<LocalDate, Long> trend) {
        List<TrendDataPointDTO> trendList = toTrendDataPoints(trend);
        return new CourseTrendDTO(trendList);
    }

    /**
     * 将课程学习人数Map转换为条目列表（按学习人数倒序）
     * @param learnersByCourse 课程ID -> 学习人数
     * @param courseTitleMap 课程ID -> 标题
     */
    public static java.util.List<CourseLearningItemDTO> toCourseLearningItems(
            Map<String, Long> learnersByCourse,
            Map<String, String> courseTitleMap) {
        return learnersByCourse.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> new CourseLearningItemDTO(
                        e.getKey(),
                        courseTitleMap.getOrDefault(e.getKey(), "未知课程"),
                        e.getValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 将Map转换为趋势数据点列表（按日期排序）
     */
    private static List<TrendDataPointDTO> toTrendDataPoints(Map<LocalDate, Long> trendMap) {
        return trendMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new TrendDataPointDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
}
