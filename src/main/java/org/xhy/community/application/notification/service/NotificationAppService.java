package org.xhy.community.application.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.notification.assembler.NotificationAssembler;
import org.xhy.community.application.notification.dto.NotificationDTO;
import org.xhy.community.domain.notification.entity.NotificationEntity;
import org.xhy.community.domain.notification.query.NotificationQuery;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.interfaces.notification.request.NotificationQueryRequest;

/**
 * 用户通知应用服务
 */
@Service
public class NotificationAppService {
    
    private final NotificationDomainService notificationDomainService;
    
    public NotificationAppService(NotificationDomainService notificationDomainService) {
        this.notificationDomainService = notificationDomainService;
    }
    
    /**
     * 获取用户通知列表
     */
    public IPage<NotificationDTO> getUserNotifications(String userId, NotificationQueryRequest request) {
        NotificationQuery query = NotificationAssembler.toQuery(request, userId);
        IPage<NotificationEntity> notifications = notificationDomainService.getUserNotifications(query);

        return NotificationAssembler.toDTOPage(notifications);
    }
    
    /**
     * 获取未读通知数量
     */
    public Long getUnreadNotificationCount(String userId) {
        return notificationDomainService.getUnreadCount(userId);
    }
    
    /**
     * 标记通知为已读
     */
    public void markNotificationAsRead(String userId, String notificationId) {
        notificationDomainService.markAsRead(userId, notificationId);
    }
    
    /**
     * 标记所有通知为已读
     */
    public void markAllNotificationsAsRead(String userId) {
        notificationDomainService.markAllAsRead(userId);
    }
}