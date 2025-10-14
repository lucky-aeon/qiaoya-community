package org.xhy.community.domain.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.jsonwebtoken.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.entity.NotificationEntity;
import org.xhy.community.domain.notification.query.NotificationQuery;
import org.xhy.community.domain.notification.repository.NotificationRepository;
import org.xhy.community.domain.notification.template.NotificationTemplate;
import org.xhy.community.domain.notification.template.NotificationTemplateRegistry;
import org.xhy.community.domain.notification.valueobject.*;
import org.xhy.community.infrastructure.email.EmailService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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


    public void send(NotificationData notificationData){
        sendToChannel(notificationData,ChannelType.IN_APP);
        sendToChannel(notificationData,ChannelType.EMAIL);
    }

    /**
     * 发送到指定渠道
     */
    private <T extends NotificationData> void sendToChannel(T notificationData, ChannelType channelType) {
        try {

            // 1. 获取模板
            NotificationTemplate<T> template = templateRegistry.getTemplate(notificationData.getContentType(), channelType);
            if (template == null) return;

            // 防御：模板数据类型与实际数据不匹配时直接跳过，避免 ClassCastException
            if (template.getSupportedDataType() != null &&
                !template.getSupportedDataType().isInstance(notificationData)) {
                log.warn("通知模板数据类型不匹配: expected={}, actual={}, contentType={}, channel={}",
                        template.getSupportedDataType().getSimpleName(),
                        notificationData.getClass().getSimpleName(),
                        notificationData.getContentType(),
                        channelType);
                return;
            }

            // 2. 渲染内容
            String title = template.renderTitle(notificationData);

            String content = template.renderContent(notificationData);

            List<NotificationData.Recipient> recipients = notificationData.getRecipients();

            ArrayList<NotificationEntity> notificationEntities = new ArrayList<>();

            ArrayList<String> notificationEmails = new ArrayList<>();

            for (NotificationData.Recipient recipient : recipients) {
                // 3. 创建通知记录
                NotificationEntity notification = new NotificationEntity();
                notification.setRecipientId(recipient.getRecipientId());
                notification.setType(notificationData.getType());
                notification.setChannelType(channelType);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setStatus(NotificationStatus.SENT);
                notificationEntities.add(notification);
                if (recipient.getEmailNotificationEnabled()){
                    if (Strings.hasText(recipient.getRecipientEmail())){
                        notificationEmails.add(recipient.getRecipientEmail());
                    }
                }
            }
            notificationRepository.insert(notificationEntities);


            if (channelType == ChannelType.EMAIL){
                sendNotificationToChannel(channelType, notificationEmails, title, content);
            }

        } catch (Exception e) {
            log.error("发送通知异常: dataType={}, channel={}",
                    notificationData.getClass().getSimpleName(), channelType, e);
        }
    }

    /**
     * 发送通知到指定渠道
     */
    private boolean sendNotificationToChannel(ChannelType channelType, List<String> address, String title, String content) {
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
    public IPage<NotificationEntity> getUserNotifications(NotificationQuery query) {
        Page<NotificationEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<NotificationEntity> queryWrapper =
            new LambdaQueryWrapper<NotificationEntity>()
                .eq(NotificationEntity::getRecipientId, query.getUserId())
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
