package org.xhy.community.application.notification.handler;

import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;

import java.util.List;

/**
 * 通知处理器接口
 * 不同类型的内容发布需要不同的通知模板和处理逻辑
 */
public interface NotificationHandler {

    /**
     * 获取处理器支持的内容类型
     */
    ContentType getSupportedContentType();

    /**
     * 处理内容发布通知
     *
     * @param contentId  内容ID
     * @param authorId   作者ID
     * @param recipients 通知接收者列表
     */
    void handleNotification(String contentId, String authorId, List<ContentNotificationService.NotificationRecipient> recipients);

    /**
     * 处理特殊场景的通知（如章节更新通知课程关注者）
     *
     * @param contentId       内容ID
     * @param authorId        作者ID
     * @param recipients      通知接收者列表
     * @param extraParameters 额外参数，用于传递特殊信息（如课程ID、章节标题等）
     */
    default void handleSpecialNotification(String contentId, String authorId,
                                         List<ContentNotificationService.NotificationRecipient> recipients,
                                         java.util.Map<String, Object> extraParameters) {
        // 默认行为：调用标准通知处理
        handleNotification(contentId, authorId, recipients);
    }
}