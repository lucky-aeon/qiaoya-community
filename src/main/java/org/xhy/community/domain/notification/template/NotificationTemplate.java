package org.xhy.community.domain.notification.template;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.NotificationData;

/**
 * 通知模板接口
 */
public interface NotificationTemplate<T extends NotificationData> {
    
    /**
     * 渲染标题
     */
    String renderTitle(T data);
    
    /**
     * 渲染内容
     */
    String renderContent(T data);
    
    /**
     * 获取支持的通知数据类型
     */
    Class<T> getSupportedDataType();

    /**
     * 模板类型
     * @return
     */
    ContentType getContentType();
}
