package org.xhy.community.application.metrics.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.metrics.assembler.MetricsAssembler;
import org.xhy.community.application.metrics.dto.*;
import org.xhy.community.domain.common.valueobject.TimeRange;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.entity.UserCourseProgressEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.service.CourseProgressDomainService;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.service.OrderDomainService;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员统计指标应用服务
 * 在Application层编排多个领域服务
 */
@Service
public class AdminMetricsAppService {

    private final UserDomainService userDomainService;
    private final UserActivityLogDomainService activityLogDomainService;
    private final OrderDomainService orderDomainService;
    private final CourseDomainService courseDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CourseProgressDomainService courseProgressDomainService;

    public AdminMetricsAppService(UserDomainService userDomainService,
                                 UserActivityLogDomainService activityLogDomainService,
                                 OrderDomainService orderDomainService,
                                 CourseDomainService courseDomainService,
                                 SubscriptionDomainService subscriptionDomainService,
                                 SubscriptionPlanDomainService subscriptionPlanDomainService,
                                 CourseProgressDomainService courseProgressDomainService) {
        this.userDomainService = userDomainService;
        this.activityLogDomainService = activityLogDomainService;
        this.orderDomainService = orderDomainService;
        this.courseDomainService = courseDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.courseProgressDomainService = courseProgressDomainService;
    }

    /**
     * 获取仪表盘所有指标
     * @param timeRange 时间范围（DAY/WEEK/MONTH）
     * @param days 查询天数（默认30天）
     */
    public DashboardMetricsDTO getDashboardMetrics(TimeRange timeRange, Integer days) {
        int queryDays = (days != null && days > 0) ? days : 30;

        // 获取各项指标
        ActiveUserTrendDTO activeUserTrend = getActiveUserTrend(timeRange, queryDays);
        OrderTrendDTO orderTrend = getOrderTrend(timeRange, queryDays);
        RegistrationTrendDTO registrationTrend = getRegistrationTrend(timeRange, queryDays);
        CourseTrendDTO courseTrend = getCourseTrend(timeRange, queryDays);

        // 课程学习指标（当期天/周/月内，学习过课程的学习人数聚合）
        java.util.List<CourseLearningItemDTO> courseLearningMetrics = getCourseLearningMetrics(timeRange);

        return new DashboardMetricsDTO(activeUserTrend, orderTrend, registrationTrend, courseTrend, courseLearningMetrics);
    }

    /**
     * 获取课程学习人数指标（只统计当前周期：当天/本周/本月）
     */
    public java.util.List<CourseLearningItemDTO> getCourseLearningMetrics(TimeRange timeRange) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startTime;
        switch (timeRange) {
            case WEEK:
                startTime = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
                break;
            case MONTH:
                startTime = today.withDayOfMonth(1).atStartOfDay();
                break;
            case DAY:
            default:
                startTime = today.atStartOfDay();
        }
        java.time.LocalDateTime endTime = java.time.LocalDateTime.now();

        // 查询时间范围内所有用户-课程聚合进度记录（代表在该周期“学习过课程”）
        java.util.List<UserCourseProgressEntity> aggregates =
                courseProgressDomainService.listAggregatesByAccessTime(startTime, endTime);

        // 课程 -> 学习人数（每条记录代表某用户在某课程有学习记录）
        java.util.Map<String, Long> learnersByCourse = aggregates.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UserCourseProgressEntity::getCourseId,
                        java.util.stream.Collectors.counting()
                ));

        // 批量获取课程标题
        java.util.Map<String, String> titleMap = courseDomainService.getCourseTitleMapByIds(learnersByCourse.keySet());

        // 装配DTO
        return MetricsAssembler.toCourseLearningItems(learnersByCourse, titleMap);
    }

    /**
     * 获取活跃用户趋势
     * @param timeRange 时间范围
     * @param days 查询天数
     */
    public ActiveUserTrendDTO getActiveUserTrend(TimeRange timeRange, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 从log领域服务获取活动日志
        List<UserActivityLogEntity> logs = activityLogDomainService.getActiveUserLogs(startTime, endTime);

        // 计算总活跃用户趋势
        Map<LocalDate, Long> totalTrend = groupByDateAndCountUsers(logs, timeRange);

        // 计算按套餐分组的活跃用户趋势
        Map<String, Map<LocalDate, Long>> subscriptionTrends =
            getActiveUserTrendBySubscription(logs, timeRange, startTime, endTime);

        return MetricsAssembler.toActiveUserTrendDTO(totalTrend, subscriptionTrends);
    }

    /**
     * 获取订单趋势
     * @param timeRange 时间范围
     * @param days 查询天数
     */
    public OrderTrendDTO getOrderTrend(TimeRange timeRange, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 从order领域服务获取订单
        List<OrderEntity> orders = orderDomainService.getOrders(startTime, endTime);

        // 计算订单数量趋势
        Map<LocalDate, Long> countTrend = groupByDateAndCountOrders(orders, timeRange);

        // 计算订单金额趋势
        Map<LocalDate, BigDecimal> amountTrend = groupByDateAndSumAmount(orders, timeRange);

        return MetricsAssembler.toOrderTrendDTO(countTrend, amountTrend);
    }

    /**
     * 获取注册用户趋势
     * @param timeRange 时间范围
     * @param days 查询天数
     */
    public RegistrationTrendDTO getRegistrationTrend(TimeRange timeRange, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 从user领域服务获取用户
        List<UserEntity> users = userDomainService.getRegisteredUsers(startTime, endTime);

        // 按日期分组统计
        Map<LocalDate, Long> trend = groupByDateAndCountEntities(
            users,
            timeRange,
            UserEntity::getCreateTime
        );

        return MetricsAssembler.toRegistrationTrendDTO(trend);
    }

    /**
     * 获取课程趋势
     * @param timeRange 时间范围
     * @param days 查询天数
     */
    public CourseTrendDTO getCourseTrend(TimeRange timeRange, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 从course领域服务获取课程
        List<CourseEntity> courses = courseDomainService.getCourses(startTime, endTime);

        // 按日期分组统计
        Map<LocalDate, Long> trend = groupByDateAndCountEntities(
            courses,
            timeRange,
            CourseEntity::getCreateTime
        );

        return MetricsAssembler.toCourseTrendDTO(trend);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 按日期分组并统计活跃用户数（去重）
     */
    private Map<LocalDate, Long> groupByDateAndCountUsers(List<UserActivityLogEntity> logs, TimeRange timeRange) {
        Map<LocalDate, Set<String>> dateToUsersMap = new HashMap<>();

        for (UserActivityLogEntity log : logs) {
            LocalDate date = convertToDate(log.getCreateTime(), timeRange);
            dateToUsersMap.computeIfAbsent(date, k -> new HashSet<>()).add(log.getUserId());
        }

        return dateToUsersMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (long) entry.getValue().size()
            ));
    }

    /**
     * 按套餐分组的活跃用户统计（Application层编排多个领域）
     */
    private Map<String, Map<LocalDate, Long>> getActiveUserTrendBySubscription(
            List<UserActivityLogEntity> logs,
            TimeRange timeRange,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        // 获取所有活跃用户ID
        Set<String> activeUserIds = logs.stream()
            .map(UserActivityLogEntity::getUserId)
            .collect(Collectors.toSet());

        if (activeUserIds.isEmpty()) {
            return new HashMap<>();
        }

        // 从subscription领域服务获取用户订阅信息
        List<UserSubscriptionEntity> subscriptions =
            subscriptionDomainService.getUserSubscriptions(activeUserIds, startTime, endTime);

        // 获取套餐信息
        Set<String> planIds = subscriptions.stream()
            .map(UserSubscriptionEntity::getSubscriptionPlanId)
            .collect(Collectors.toSet());

        Map<String, SubscriptionPlanEntity> planMap = new HashMap<>();
        if (!planIds.isEmpty()) {
            List<SubscriptionPlanEntity> plans = subscriptionPlanDomainService.getSubscriptionPlansByIds(planIds);
            planMap = plans.stream().collect(Collectors.toMap(SubscriptionPlanEntity::getId, p -> p));
        }

        // 构建用户ID到套餐名称的映射
        Map<String, String> userToPlanMap = new HashMap<>();
        for (UserSubscriptionEntity sub : subscriptions) {
            SubscriptionPlanEntity plan = planMap.get(sub.getSubscriptionPlanId());
            if (plan != null) {
                userToPlanMap.put(sub.getUserId(), plan.getName());
            }
        }

        // 按套餐分组日志
        Map<String, List<UserActivityLogEntity>> logsByPlan = new HashMap<>();
        for (UserActivityLogEntity log : logs) {
            String planName = userToPlanMap.getOrDefault(log.getUserId(), "无套餐");
            logsByPlan.computeIfAbsent(planName, k -> new ArrayList<>()).add(log);
        }

        // 为每个套餐生成趋势
        Map<String, Map<LocalDate, Long>> result = new HashMap<>();
        for (Map.Entry<String, List<UserActivityLogEntity>> entry : logsByPlan.entrySet()) {
            result.put(entry.getKey(), groupByDateAndCountUsers(entry.getValue(), timeRange));
        }

        return result;
    }

    /**
     * 按日期分组并统计订单数量
     */
    private Map<LocalDate, Long> groupByDateAndCountOrders(List<OrderEntity> orders, TimeRange timeRange) {
        return orders.stream()
            .collect(Collectors.groupingBy(
                order -> convertToDate(order.getActivatedTime(), timeRange),
                Collectors.counting()
            ));
    }

    /**
     * 按日期分组并统计订单金额
     */
    private Map<LocalDate, BigDecimal> groupByDateAndSumAmount(List<OrderEntity> orders, TimeRange timeRange) {
        return orders.stream()
            .collect(Collectors.groupingBy(
                order -> convertToDate(order.getActivatedTime(), timeRange),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderEntity::getAmount,
                    BigDecimal::add
                )
            ));
    }

    /**
     * 按日期分组并统计实体数量（通用）
     */
    private <T> Map<LocalDate, Long> groupByDateAndCountEntities(
            List<T> entities,
            TimeRange timeRange,
            java.util.function.Function<T, LocalDateTime> timeExtractor) {

        return entities.stream()
            .collect(Collectors.groupingBy(
                entity -> convertToDate(timeExtractor.apply(entity), timeRange),
                Collectors.counting()
            ));
    }

    /**
     * 将时间转换为日期（根据TimeRange）
     */
    private LocalDate convertToDate(LocalDateTime dateTime, TimeRange timeRange) {
        if (dateTime == null) {
            return LocalDate.now();
        }

        LocalDate date = dateTime.toLocalDate();

        switch (timeRange) {
            case WEEK:
                // 返回当周的周一
                return date.minusDays(date.getDayOfWeek().getValue() - 1);
            case MONTH:
                // 返回当月第一天
                return date.withDayOfMonth(1);
            case DAY:
            default:
                return date;
        }
    }
}
