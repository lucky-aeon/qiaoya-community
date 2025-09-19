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
import org.xhy.community.domain.subscription.event.SubscriptionExpiringEvent;

/**
 * CDK和订阅相关事件监听器 - Application层负责流程编排
 * 位置：org.xhy.community.application.notification.listener.SubscriptionNotificationListener
 */
@Component
public class SubscriptionNotificationListener {
    
    private static final Logger log = LoggerFactory.getLogger(SubscriptionNotificationListener.class);
    
    private final NotificationDomainService notificationDomainService;
    
    public SubscriptionNotificationListener(NotificationDomainService notificationDomainService) {
        this.notificationDomainService = notificationDomainService;
    }
    
    /**
     * 监听CDK激活事件 - Application层流程编排
     */
    @EventListener
    @Async
    public void handleCDKActivatedEvent(CDKActivatedEvent event) {
        try {
            // Application层流程编排：从事件构建通知数据
            CDKActivatedNotificationData notificationData = 
                new CDKActivatedNotificationData(
                    event.getUserId(),
                    event.getUserName(),
                    event.getUserEmail(),
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
     * 监听订阅即将过期事件
     */
    @EventListener
    @Async  
    public void handleSubscriptionExpiringEvent(SubscriptionExpiringEvent event) {
        try {
            // Application层流程编排：从事件构建通知数据
            SubscriptionExpiredNotificationData notificationData = 
                new SubscriptionExpiredNotificationData(
                    event.getUserId(),
                    event.getUserName(),
                    event.getUserEmail(),
                    event.getSubscriptionId(),
                    event.getDaysRemaining()
                );
            
            // Application层职责：调用Domain服务
            notificationDomainService.sendNotification(notificationData);
            
        } catch (Exception e) {
            log.error("处理订阅即将过期事件失败", e);
        }
    }
}