package org.xhy.community.domain.notification.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.valueobject.ChannelType;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知模板注册器
 */
@Service
public class NotificationTemplateRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateRegistry.class);
    
    // 站内消息模板
    private final Map<Class<? extends NotificationData>, NotificationTemplate> inAppTemplates = new HashMap<>();
    
    // 站外消息模板
    private final Map<Class<? extends NotificationData>, NotificationTemplate> outAppTemplates = new HashMap<>();
    
    @PostConstruct
    public void initTemplates() {
        // 注册站内消息模板
        registerInAppTemplate(new InAppNotificationTemplates.NewFollowerTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.ContentUpdateTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.CDKActivatedTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.SubscriptionExpiredTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.CommentTemplate());
        
        // 注册站外消息模板
        registerOutAppTemplate(new OutAppNotificationTemplates.NewFollowerTemplate());
        registerOutAppTemplate(new OutAppNotificationTemplates.ContentUpdateTemplate());
        registerOutAppTemplate(new OutAppNotificationTemplates.CDKActivatedTemplate());
        registerOutAppTemplate(new OutAppNotificationTemplates.SubscriptionExpiredTemplate());
        registerOutAppTemplate(new OutAppNotificationTemplates.CommentTemplate());
        
        log.info("通知模板注册完成: 站内模板{}个, 站外模板{}个", 
                inAppTemplates.size(), outAppTemplates.size());
    }
    
    /**
     * 注册站内消息模板
     */
    private void registerInAppTemplate(NotificationTemplate template) {
        inAppTemplates.put(template.getSupportedDataType(), template);
    }
    
    /**
     * 注册站外消息模板
     */
    private void registerOutAppTemplate(NotificationTemplate template) {
        outAppTemplates.put(template.getSupportedDataType(), template);
    }
    
    /**
     * 获取站内消息模板
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getInAppTemplate(Class<T> dataType) {
        return inAppTemplates.get(dataType);
    }
    
    /**
     * 获取站外消息模板
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getOutAppTemplate(Class<T> dataType) {
        return outAppTemplates.get(dataType);
    }
    
    /**
     * 获取模板（根据渠道类型）
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getTemplate(Class<T> dataType, ChannelType channelType) {
        if (channelType == ChannelType.IN_APP) {
            return getInAppTemplate(dataType);
        } else {
            return getOutAppTemplate(dataType);
        }
    }
    
    /**
     * 检查是否支持指定类型和渠道的模板
     */
    public boolean hasTemplate(Class<? extends NotificationData> dataType, ChannelType channelType) {
        return getTemplate(dataType, channelType) != null;
    }
}