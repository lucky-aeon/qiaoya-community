package org.xhy.community.application.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.notification.context.CDKActivatedNotificationData;
import org.xhy.community.domain.notification.context.SubscriptionExpiredNotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.valueobject.BatchSendConfig;
import org.xhy.community.domain.notification.valueobject.BatchSendResult;
import org.xhy.community.domain.subscription.event.SubscriptionExpiringEvent;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CDK和订阅相关事件监听器 - Application层负责流程编排
 * 位置：org.xhy.community.application.notification.listener.SubscriptionNotificationListener
 */
@Component
public class SubscriptionNotificationListener {
    
    private static final Logger log = LoggerFactory.getLogger(SubscriptionNotificationListener.class);
    
    private final NotificationDomainService notificationDomainService;
    private final UserDomainService userDomainService;

    public SubscriptionNotificationListener(NotificationDomainService notificationDomainService,
                                          UserDomainService userDomainService) {
        this.notificationDomainService = notificationDomainService;
        this.userDomainService = userDomainService;
    }
    
    /**
     * 监听CDK激活事件 - Application层流程编排
     */
    @EventListener
    @Async
    public void handleCDKActivatedEvent(CDKActivatedEvent event) {
        try {
            // Application层流程编排：查询用户邮箱开关设置
            Boolean emailNotificationEnabled = userDomainService.getUserById(event.getUserId())
                .getEmailNotificationEnabled();

            // Application层流程编排：从事件构建通知数据
            CDKActivatedNotificationData notificationData =
                new CDKActivatedNotificationData(
                    event.getUserId(),
                    event.getUserName(),
                    event.getUserEmail(),
                    emailNotificationEnabled,
                    event.getCdkCode(),
                    event.getActivatedTime()
                );

            // Application层职责：调用Domain服务
            notificationDomainService.sendNotification(notificationData);

        } catch (Exception e) {
            log.error("处理CDK激活事件失败", e);
        }
    }
    
    /**
     * 定时检查订阅过期 - 发送事件驱动的通知
     * 注意：实际的检查逻辑应该在subscription领域中，这里只是示例
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天早上9点执行
    public void checkExpiringSubscriptions() {
        try {
            // 这里需要调用订阅领域服务获取即将过期的订阅，并发布事件
            // 然后监听该事件进行通知
            log.info("开始检查即将过期的订阅");
            
        } catch (Exception e) {
            log.error("检查订阅过期状态失败", e);
        }
    }
    
    /**
     * 监听订阅即将过期批量事件 - 支持批量处理
     */
    @EventListener
    @Async
    public void handleSubscriptionExpiringEvent(SubscriptionExpiringEvent event) {
        try {
            List<SubscriptionExpiringEvent.ExpiringSubscription> subscriptions = event.getSubscriptions();

            log.info("[订阅过期通知] 开始处理批量订阅过期通知: 数量={}, 检查时间={}",
                    event.getTotalCount(), event.getCheckTime());

            if (subscriptions == null || subscriptions.isEmpty()) {
                log.warn("[订阅过期通知] 没有需要处理的订阅");
                return;
            }

            // 1. 批量查询用户邮箱开关设置（避免N+1查询问题）
            Set<String> userIds = subscriptions.stream()
                    .map(SubscriptionExpiringEvent.ExpiringSubscription::getUserId)
                    .collect(Collectors.toSet());

            Map<String, Boolean> emailSettingsMap = userDomainService.getUserEmailSettingsByIds(userIds);
            log.info("[订阅过期通知] 批量查询用户邮箱设置完成: 用户数={}", emailSettingsMap.size());

            // 2. 构建批量通知数据
            List<SubscriptionExpiredNotificationData> notifications = subscriptions.stream()
                    .map(sub -> new SubscriptionExpiredNotificationData(
                            sub.getUserId(),
                            sub.getUserName(),
                            sub.getUserEmail(),
                            emailSettingsMap.getOrDefault(sub.getUserId(), false), // 默认关闭邮箱通知
                            sub.getSubscriptionId(),
                            sub.getDaysRemaining()
                    ))
                    .collect(Collectors.toList());

            // 3. 配置批量发送参数
            BatchSendConfig config = new BatchSendConfig()
                    .withBatchSize(50)              // 每批50个
                    .withDelayBetweenBatches(2000)  // 批次间隔2秒
                    .withSkipOnError(true)          // 出错继续处理
                    .withLogDetail(true)            // 记录详细日志
                    .withMaxRetries(0);             // 暂不重试

            log.info("[订阅过期通知] 批量发送配置: 批大小={}, 延迟={}ms, 跳过错误={}",
                    config.getBatchSize(), config.getDelayBetweenBatches(), config.isSkipOnError());

            // 4. 执行批量发送
            BatchSendResult result = notificationDomainService.sendBatchNotifications(notifications, config);

            // 5. 记录统计结果
            log.info("[订阅过期通知] 发送统计: 总计={}, 成功={}, 失败={}, 跳过={}, 耗时={}ms, 成功率={:.1f}%",
                    result.getTotalCount(),
                    result.getSuccessCount(),
                    result.getFailedCount(),
                    result.getSkippedCount(),
                    result.getTotalTimeMs(),
                    result.getSuccessRate());

            // 6. 处理失败项（记录详细失败信息）
            if (result.getFailedCount() > 0) {
                log.warn("[订阅过期通知] 存在发送失败项: 失败数={}", result.getFailedCount());
                for (BatchSendResult.FailedItem failedItem : result.getFailedItems()) {
                    log.error("[订阅过期通知] 发送失败详情: userId={}, email={}, reason={}, time={}",
                            failedItem.getUserId(), failedItem.getEmail(),
                            failedItem.getReason(), failedItem.getFailTime());
                }
            }

        } catch (Exception e) {
            log.error("[订阅过期通知] 批量订阅过期通知处理失败", e);
        }
    }
}