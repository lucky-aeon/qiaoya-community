package org.xhy.community.application.subscription.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.Duration;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.user.event.UserRegisteredEvent;
import org.xhy.community.domain.user.event.UserLoginEvent;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.infrastructure.lock.DistributedLock;

/**
 * 默认套餐分配监听器
 * 监听用户注册和登录事件，自动为用户分配默认免费套餐
 * - 注册时：为新用户分配默认套餐
 * - 登录时：为没有有效套餐的用户兜底分配默认套餐
 */
@Component
public class DefaultSubscriptionEventListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultSubscriptionEventListener.class);

    private final SystemConfigDomainService systemConfigDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final DistributedLock distributedLock;

    public DefaultSubscriptionEventListener(SystemConfigDomainService systemConfigDomainService,
                                            SubscriptionDomainService subscriptionDomainService,
                                            DistributedLock distributedLock) {
        this.systemConfigDomainService = systemConfigDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.distributedLock = distributedLock;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            // 注册事件为常规流程，省略日志

            String userId = event.getUserId();
            String lockKey = "lock:default_subscription:" + userId;
            // 等待 200ms，租约 3s，串行化同一用户的套餐初始化
            distributedLock.runWithLock(lockKey, Duration.ofMillis(200), Duration.ofSeconds(3), () -> {
                // 加锁内再次获取配置，保证一致性
                DefaultSubscriptionConfig config = systemConfigDomainService.getDefaultSubscriptionConfig();
                if (config == null || !config.isValid()) {
                    log.warn("[注册] 默认套餐配置无效或未启用，跳过为用户 {} 分配默认套餐", userId);
                    return;
                }

                // 已有有效套餐则跳过（避免重复创建）
                if (subscriptionDomainService.hasAnyActiveSubscription(userId)) {
                    // 已有有效套餐，省略日志
                    return;
                }

                // 再次兜底校验：相同默认套餐是否已存在（并发边界情况下）
                if (subscriptionDomainService.checkActiveSubscriptionExists(userId, config.getSubscriptionPlanId())) {
                    // 已拥有默认套餐，省略日志
                    return;
                }

                // 创建系统赠送的免费订阅
                subscriptionDomainService.createSystemGiftSubscription(userId, config.getSubscriptionPlanId());
                // 分配默认套餐成功，省略日志
            });

        } catch (Exception e) {
            log.error("为用户 {} 分配默认套餐失败", event.getUserId(), e);
            // 不抛出异常，避免影响用户注册流程
        }
    }

    @EventListener
    public void handleUserLogin(UserLoginEvent event) {
        try {
            // 登录事件为常规流程，省略日志

            String userId = event.getUserId();
            String lockKey = "lock:default_subscription:" + userId;
            distributedLock.runWithLock(lockKey, Duration.ofMillis(200), Duration.ofSeconds(3), () -> {
                DefaultSubscriptionConfig config = systemConfigDomainService.getDefaultSubscriptionConfig();
                if (config == null || !config.isValid()) {
            log.warn("默认套餐配置无效或未启用，跳过为用户 {} 分配默认套餐", userId);
            return;
                }

                // 登录兜底：仅当没有任何有效套餐时才创建默认套餐
                if (subscriptionDomainService.hasAnyActiveSubscription(userId)) {
                    return;
                }

                if (subscriptionDomainService.checkActiveSubscriptionExists(userId, config.getSubscriptionPlanId())) {
                    return;
                }

                UserSubscriptionEntity result = subscriptionDomainService.createSystemGiftSubscription(
                        userId,
                        config.getSubscriptionPlanId()
                );

                // 兜底分配成功或跳过，省略日志
            });

        } catch (Exception e) {
            log.error("为登录用户 {} 兜底分配默认套餐失败", event.getUserId(), e);
            // 不抛出异常，避免影响用户登录流程
        }
    }
}
