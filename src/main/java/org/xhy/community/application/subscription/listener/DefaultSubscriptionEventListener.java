package org.xhy.community.application.subscription.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.user.event.UserRegisteredEvent;

/**
 * 默认套餐分配监听器
 * 监听用户注册事件，自动为新用户分配默认免费套餐
 */
@Component
public class DefaultSubscriptionEventListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultSubscriptionEventListener.class);

    private final SystemConfigDomainService systemConfigDomainService;
    private final SubscriptionDomainService subscriptionDomainService;

    public DefaultSubscriptionEventListener(SystemConfigDomainService systemConfigDomainService,
                                          SubscriptionDomainService subscriptionDomainService) {
        this.systemConfigDomainService = systemConfigDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("监听到用户注册事件，用户ID: {}, 邮箱: {}", event.getUserId(), event.getEmail());

            // 获取默认套餐配置
            DefaultSubscriptionConfig config = systemConfigDomainService.getDefaultSubscriptionConfig();

            if (config == null || !config.isValid()) {
                log.warn("默认套餐配置无效或未启用，跳过为用户 {} 分配默认套餐", event.getUserId());
                return;
            }

            // 为新用户创建系统赠送的免费订阅
            subscriptionDomainService.createSystemGiftSubscription(
                    event.getUserId(),
                    config.getSubscriptionPlanId()
            );

            log.info("成功为新用户 {} 分配默认套餐: {}", event.getUserId(), config.getSubscriptionPlanId());

        } catch (Exception e) {
            log.error("为用户 {} 分配默认套餐失败", event.getUserId(), e);
            // 不抛出异常，避免影响用户注册流程
        }
    }
}