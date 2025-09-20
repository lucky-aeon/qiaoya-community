package org.xhy.community.application.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.comment.event.CommentCreatedEvent;
import org.xhy.community.domain.notification.context.CommentNotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.user.service.UserDomainService;

/**
 * 评论相关事件监听器 - Application层负责流程编排
 * 位置：org.xhy.community.application.notification.listener.CommentNotificationListener
 */
@Component
public class CommentNotificationListener {
    
    private static final Logger log = LoggerFactory.getLogger(CommentNotificationListener.class);
    
    private final NotificationDomainService notificationDomainService;
    private final UserDomainService userDomainService;

    public CommentNotificationListener(NotificationDomainService notificationDomainService,
                                     UserDomainService userDomainService) {
        this.notificationDomainService = notificationDomainService;
        this.userDomainService = userDomainService;
    }
    
    /**
     * 监听评论创建事件 - Application层流程编排
     */
    @EventListener
    @Async
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        try {
            // Application层流程编排：业务规则判断
            // 不给自己发通知
            if (event.getCommenterId().equals(event.getTargetAuthorId())) {
                return;
            }
            
            // Application层流程编排：查询目标作者的邮箱开关设置
            Boolean emailNotificationEnabled = userDomainService.getUserById(event.getTargetAuthorId())
                .getEmailNotificationEnabled();

            // Application层流程编排：从事件构建通知数据
            CommentNotificationData notificationData =
                new CommentNotificationData(
                    event.getTargetAuthorId(),
                    event.getTargetAuthorName(),
                    event.getTargetAuthorEmail(),
                    emailNotificationEnabled,
                    event.getCommenterName(),
                    event.getTargetTitle(),
                    event.getTargetType(),
                    event.getCommentContent(),
                    event.getTargetId()
                );
            
            // Application层职责：调用Domain服务
            notificationDomainService.sendNotification(notificationData);
            
        } catch (Exception e) {
            log.error("处理评论事件失败", e);
        }
    }
}