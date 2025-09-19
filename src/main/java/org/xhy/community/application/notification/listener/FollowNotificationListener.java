package org.xhy.community.application.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.follow.event.ContentUpdatedEvent;
import org.xhy.community.domain.follow.event.UserFollowedEvent;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.context.NewFollowerNotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;

/**
 * 关注相关事件监听器 - Application层负责流程编排
 * 位置：org.xhy.community.application.notification.listener.FollowNotificationListener
 */
@Component
public class FollowNotificationListener {
    
    private static final Logger log = LoggerFactory.getLogger(FollowNotificationListener.class);
    
    private final NotificationDomainService notificationDomainService;
    
    public FollowNotificationListener(NotificationDomainService notificationDomainService) {
        this.notificationDomainService = notificationDomainService;
    }
    
    /**
     * 监听新关注者事件 - Application层流程编排
     * 职责：1. 监听事件 2. 流程编排 3. 调用Domain服务
     */
    @EventListener
    @Async
    public void handleUserFollowedEvent(UserFollowedEvent event) {
        try {
            // Application层流程编排：从事件构建通知数据
            NewFollowerNotificationData notificationData = 
                new NewFollowerNotificationData(
                    event.getTargetId(),
                    event.getTargetName(), 
                    event.getTargetEmail(),
                    event.getFollowerId(),
                    event.getFollowerName()
                );
            
            // Application层职责：调用Domain服务完成业务操作
            notificationDomainService.sendNotification(notificationData);
            
        } catch (Exception e) {
            log.error("处理新关注者事件失败", e);
        }
    }
    
    /**
     * 监听内容更新事件 - Application层流程编排
     */
    @EventListener
    @Async
    public void handleContentUpdatedEvent(ContentUpdatedEvent event) {
        try {
            // Application层流程编排：为每个关注者创建通知数据
            for (ContentUpdatedEvent.FollowerInfo follower : event.getFollowers()) {
                ContentUpdateNotificationData notificationData = 
                    new ContentUpdateNotificationData(
                        follower.getFollowerId(),
                        follower.getFollowerName(),
                        follower.getFollowerEmail(),
                        event.getAuthorName(),
                        event.getContentTitle(),
                        event.getContentType(),
                        event.getContentId()
                    );
                
                // Application层职责：调用Domain服务
                notificationDomainService.sendNotification(notificationData);
            }
            
        } catch (Exception e) {
            log.error("处理内容更新事件失败", e);
        }
    }
}