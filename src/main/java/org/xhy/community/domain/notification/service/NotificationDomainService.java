package org.xhy.community.domain.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.entity.NotificationEntity;
import org.xhy.community.domain.notification.repository.NotificationRepository;
import org.xhy.community.domain.notification.template.NotificationTemplate;
import org.xhy.community.domain.notification.template.NotificationTemplateRegistry;
import org.xhy.community.domain.notification.valueobject.ChannelType;
import org.xhy.community.domain.notification.valueobject.NotificationStatus;
import org.xhy.community.infrastructure.email.EmailService;

import java.util.UUID;

/**
 * 纯粹的通知领域服务 - 不依赖任何其他领域
 */
@Service
public class NotificationDomainService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationDomainService.class);
    
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRegistry templateRegistry;
    private final EmailService emailService;

    public NotificationDomainService(NotificationRepository notificationRepository,
                                   NotificationTemplateRegistry templateRegistry,
                                   EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.templateRegistry = templateRegistry;
        this.emailService = emailService;
    }
    
    /**
     * 发送通知 - 所有需要的数据都在notificationData中
     */
    public <T extends NotificationData> void sendNotification(T notificationData) {
        Class<T> dataType = (Class<T>) notificationData.getClass();
        
        // 发送站内消息
        if (templateRegistry.hasTemplate(dataType, ChannelType.IN_APP)) {
            sendToChannel(notificationData, ChannelType.IN_APP);
        }
        
        // 发送邮件
        if (templateRegistry.hasTemplate(dataType, ChannelType.EMAIL)) {
            sendToChannel(notificationData, ChannelType.EMAIL);
        }
    }
    
    /**
     * 发送到指定渠道
     */
    private <T extends NotificationData> void sendToChannel(T notificationData, ChannelType channelType) {
        try {
            Class<T> dataType = (Class<T>) notificationData.getClass();
            
            // 1. 获取模板
            NotificationTemplate<T> template = templateRegistry.getTemplate(dataType, channelType);
            if (template == null) return;
            
            // 2. 渲染内容
            String title = template.renderTitle(notificationData);
            String content = template.renderContent(notificationData);
            
            // 3. 创建通知记录
            NotificationEntity notification = new NotificationEntity();
            notification.setRecipientId(notificationData.getRecipientId());
            notification.setType(notificationData.getType());
            notification.setChannelType(channelType);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setStatus(NotificationStatus.PENDING);
            
            // 4. 获取渠道地址（从通知数据中获取，不查询其他领域）
            String recipientAddress = getChannelAddress(notificationData, channelType);
            if (recipientAddress == null) {
                notification.markAsFailed();
                notificationRepository.insert(notification);
                log.warn("无法获取渠道地址: userId={}, channel={}", 
                        notificationData.getRecipientId(), channelType);
                return;
            }
            
            // 5. 发送通知
            boolean sendSuccess = sendNotificationToChannel(channelType, recipientAddress, title, content);
            
            if (sendSuccess) {
                notification.markAsSent();
                log.info("通知发送成功: userId={}, channel={}, type={}", 
                        notificationData.getRecipientId(), channelType, notificationData.getType());
            } else {
                notification.markAsFailed();
                log.error("通知发送失败: userId={}, channel={}", 
                         notificationData.getRecipientId(), channelType);
            }
            
            // 6. 保存记录
            notificationRepository.insert(notification);
            
        } catch (Exception e) {
            log.error("发送通知异常: dataType={}, channel={}", 
                     notificationData.getClass().getSimpleName(), channelType, e);
        }
    }
    
    /**
     * 从通知数据中获取渠道地址 - 不依赖其他领域
     */
    private String getChannelAddress(NotificationData data, ChannelType channelType) {
        switch (channelType) {
            case IN_APP:
                return data.getRecipientId();
            case EMAIL:
                return data.getRecipientEmail(); // 直接从事件数据中获取
            case SMS:
                // 需要在NotificationData中添加手机号字段
                return null; // 暂不支持短信
            default:
                return null;
        }
    }
    
    /**
     * 发送通知到指定渠道
     */
    private boolean sendNotificationToChannel(ChannelType channelType, String address, String title, String content) {
        switch (channelType) {
            case IN_APP:
                // 站内消息直接保存到数据库即算发送成功
                return true;

            case EMAIL:
                // 使用真实的邮件服务发送
                if (emailService.isEnabled()) {
                    return emailService.sendEmail(address, title, content);
                } else {
                    log.warn("邮件服务未启用，跳过邮件发送: to={}", address);
                    return false;
                }

            case SMS:
                // 暂不支持短信
                log.info("暂不支持短信发送: to={}", address);
                return false;

            default:
                log.warn("不支持的通知渠道: {}", channelType);
                return false;
        }
    }
    
    /**
     * 获取用户站内通知列表
     */
    public IPage<NotificationEntity> getUserNotifications(String userId, Integer pageNum, Integer pageSize) {
        Page<NotificationEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<NotificationEntity> queryWrapper = 
            new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getRecipientId, userId)
                .eq(NotificationEntity::getChannelType, ChannelType.IN_APP)
                .orderByDesc(NotificationEntity::getCreateTime);
        
        return notificationRepository.selectPage(page, queryWrapper);
    }
    
    /**
     * 获取未读通知数量
     */
    public Long getUnreadCount(String userId) {
        LambdaQueryWrapper<NotificationEntity> queryWrapper = 
            new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getRecipientId, userId)
                .eq(NotificationEntity::getChannelType, ChannelType.IN_APP)
                .eq(NotificationEntity::getStatus, NotificationStatus.SENT);
        
        return notificationRepository.selectCount(queryWrapper);
    }
    
    /**
     * 标记通知为已读
     */
    public void markAsRead(String userId, String notificationId) {
        NotificationEntity notification = notificationRepository.selectById(notificationId);
        if (notification != null && notification.getRecipientId().equals(userId)) {
            notification.markAsRead();
            notificationRepository.updateById(notification);
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    public void markAllAsRead(String userId) {
        LambdaUpdateWrapper<NotificationEntity> updateWrapper = 
            new LambdaUpdateWrapper<NotificationEntity>()
                .eq(NotificationEntity::getRecipientId, userId)
                .eq(NotificationEntity::getChannelType, ChannelType.IN_APP)
                .eq(NotificationEntity::getStatus, NotificationStatus.SENT)
                .set(NotificationEntity::getStatus, NotificationStatus.READ);
        
        notificationRepository.update(null, updateWrapper);
    }
}