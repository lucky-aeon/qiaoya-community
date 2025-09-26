package org.xhy.community.application.notification.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.handler.NotificationHandler;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.event.ContentPublishedEvent;
import org.xhy.community.domain.common.valueobject.ContentType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 内容事件调度器
 * 统一监听内容发布事件，根据内容类型分发到对应的通知处理器
 */
@Component
public class ContentEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ContentEventDispatcher.class);

    private final ContentNotificationService contentNotificationService;
    private final Map<ContentType, NotificationHandler> handlerMap;

    public ContentEventDispatcher(ContentNotificationService contentNotificationService,
                                List<NotificationHandler> handlers) {
        this.contentNotificationService = contentNotificationService;
        // 构建处理器映射，每种内容类型对应一个处理器
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                    NotificationHandler::getSupportedContentType,
                    Function.identity()
                ));

        log.info("ContentEventDispatcher initialized with {} handlers: {}",
                handlerMap.size(), handlerMap.keySet());
    }

    /**
     * 监听内容发布事件
     * 异步处理，不阻塞主业务流程
     */
    @EventListener
    @Async
    public void handleContentPublishedEvent(ContentPublishedEvent event) {
        try {
            log.debug("Processing ContentPublishedEvent: {}", event);

            // 根据内容类型获取对应的处理器
            NotificationHandler handler = handlerMap.get(event.getContentType());
            if (handler == null) {
                log.warn("No handler found for content type: {}", event.getContentType());
                return;
            }

            // 查询关注者信息
            List<ContentNotificationService.NotificationRecipient> recipients =
                contentNotificationService.getContentFollowers(
                    event.getContentType(),
                    event.getContentId(),
                    event.getAuthorId()
                );

            if (recipients.isEmpty()) {
                log.debug("No followers found for content {} of type {}",
                         event.getContentId(), event.getContentType());
                return;
            }

            log.debug("Found {} recipients for content {} of type {}",
                     recipients.size(), event.getContentId(), event.getContentType());

            // 委托给具体的处理器处理通知
            handler.handleNotification(event.getContentId(), event.getAuthorId(), recipients);

            log.debug("Successfully processed ContentPublishedEvent for content {} of type {}",
                     event.getContentId(), event.getContentType());

        } catch (Exception e) {
            // 记录错误但不重新抛出异常，避免影响主业务流程
            log.error("Failed to process ContentPublishedEvent: {}", event, e);
        }
    }
}