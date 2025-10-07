package org.xhy.community.domain.notification.template;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.NotificationData;

/**
 * 通知模板接口
 */
public interface NotificationTemplate<NotificationData> {
    
    /**
     * 渲染标题
     */
    String renderTitle(NotificationData data);
    
    /**
     * 渲染内容
     */
    String renderContent(NotificationData data);
    
    /**
     * 获取支持的通知数据类型
     */
    Class<NotificationData> getSupportedDataType();

    /**
     * 模板类型
     * @return
     */
    ContentType getContentType();
}
