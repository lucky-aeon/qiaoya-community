package org.xhy.community.application.subscription.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.event.SubscriptionExpiringEvent;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.user.service.UserDomainService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订阅过期检查服务 - 定时任务触发批量检查
 */
@Service
public class SubscriptionExpiryCheckService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryCheckService.class);

    private final SubscriptionDomainService subscriptionDomainService;
    private final UserDomainService userDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SubscriptionExpiryCheckService(SubscriptionDomainService subscriptionDomainService,
                                        UserDomainService userDomainService,
                                        ApplicationEventPublisher applicationEventPublisher) {
        this.subscriptionDomainService = subscriptionDomainService;
        this.userDomainService = userDomainService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 定时检查即将过期的订阅
     * 每天早上9点执行一次
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExpiringSubscriptions() {
        log.info("[订阅过期检查] 开始检查即将过期的订阅");

        try {
            // 1. 查询即将过期的订阅（如7天内过期）
            List<UserSubscriptionEntity> expiringList = findExpiringSubscriptions(7);

            if (expiringList.isEmpty()) {
                log.info("[订阅过期检查] 没有即将过期的订阅");
                return;
            }

            log.info("[订阅过期检查] 发现即将过期的订阅: 数量={}", expiringList.size());

            // 2. 构建批量事件数据
            List<SubscriptionExpiringEvent.ExpiringSubscription> subscriptions =
                buildExpiringSubscriptions(expiringList);

            // 3. 发布批量事件（只发一次事件，包含所有即将过期的订阅）
            SubscriptionExpiringEvent event = new SubscriptionExpiringEvent(
                subscriptions,
                LocalDateTime.now()
            );

            applicationEventPublisher.publishEvent(event);

            log.info("[订阅过期检查] 发布订阅过期批量事件: 数量={}", subscriptions.size());

        } catch (Exception e) {
            log.error("[订阅过期检查] 检查过程失败", e);
        }
    }

    /**
     * 查询即将过期的订阅
     * 这里是示例实现，实际需要根据具体的查询逻辑调用 SubscriptionDomainService
     */
    private List<UserSubscriptionEntity> findExpiringSubscriptions(int daysAhead) {
        // 这里应该调用 subscriptionDomainService 的相应方法
        // 暂时返回空列表作为示例
        return List.of();

        // 实际实现可能如下：
        // LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(daysAhead);
        // return subscriptionDomainService.findSubscriptionsExpiringBefore(expiryThreshold);
    }

    /**
     * 构建批量事件数据
     */
    private List<SubscriptionExpiringEvent.ExpiringSubscription> buildExpiringSubscriptions(
            List<UserSubscriptionEntity> expiringList) {

        return expiringList.stream()
            .map(subscription -> {
                // 计算剩余天数
                long daysRemaining = ChronoUnit.DAYS.between(
                    LocalDateTime.now(),
                    subscription.getEndTime()
                );

                // 获取用户信息（这里简化处理，实际可能需要批量查询）
                // UserEntity user = userDomainService.getUserById(subscription.getUserId());

                return new SubscriptionExpiringEvent.ExpiringSubscription(
                    subscription.getUserId(),
                    "用户姓名", // user.getName(),
                    "user@example.com", // user.getEmail(),
                    subscription.getId(),
                    daysRemaining,
                    subscription.getEndTime()
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * 手动触发检查（用于测试）
     */
    public void manualCheck() {
        log.info("[订阅过期检查] 手动触发检查");
        checkExpiringSubscriptions();
    }
}