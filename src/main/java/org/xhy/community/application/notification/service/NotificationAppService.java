package org.xhy.community.application.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(NotificationAppService.class);

    private final NotificationDomainService notificationDomainService;
    
    public NotificationAppService(NotificationDomainService notificationDomainService) {
        this.notificationDomainService = notificationDomainService;
    }
    
    /**
     * 获取用户通知列表
     */
    public IPage<NotificationDTO> getUserNotifications(String userId, NotificationQueryRequest request) {
        log.info("[通知-App] 列表查询开始 userId={} pageNum={} pageSize={}", userId, request.getPageNum(), request.getPageSize());
        NotificationQuery query = NotificationAssembler.toQuery(request, userId);
        IPage<NotificationEntity> notifications = notificationDomainService.getUserNotifications(query);
        IPage<NotificationDTO> dtoPage = NotificationAssembler.toDTOPage(notifications);
        log.info("[通知-App] 列表查询完成 userId={} 返回记录数={} 总数={}", userId, dtoPage.getRecords().size(), dtoPage.getTotal());
        return dtoPage;
    }
    
    /**
     * 获取未读通知数量
     */
    public Long getUnreadNotificationCount(String userId) {
        Long count = notificationDomainService.getUnreadCount(userId);
        log.info("[通知-App] 未读数查询 userId={} count={}", userId, count);
        return count;
    }
    
    /**
     * 标记通知为已读
     */
    public void markNotificationAsRead(String userId, String notificationId) {
        notificationDomainService.markAsRead(userId, notificationId);
        log.info("[通知-App] 标记已读 userId={} notificationId={}", userId, notificationId);
    }
    
    /**
     * 标记所有通知为已读
     */
    public void markAllNotificationsAsRead(String userId) {
        notificationDomainService.markAllAsRead(userId);
        log.info("[通知-App] 全部标记已读 userId={}", userId);
    }
}
