package org.xhy.community.application.notification.handler;

import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.user.entity.UserEntity;

import java.util.List;

/**
 * 文章发布通知处理器
 * 处理文章发布时的通知逻辑和消息模板
 */
@Component
public class PostNotificationHandler implements NotificationHandler {

    private final PostDomainService postDomainService;
    private final UserDomainService userDomainService;
    private final NotificationDomainService notificationDomainService;

    public PostNotificationHandler(PostDomainService postDomainService,
                                 UserDomainService userDomainService,
                                 NotificationDomainService notificationDomainService) {
        this.postDomainService = postDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
    }

    @Override
    public ContentType getSupportedContentType() {
        return ContentType.POST;
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                 List<ContentNotificationService.NotificationRecipient> recipients) {
        try {
            // 获取文章信息
            PostEntity post = postDomainService.getPostById(contentId);
            UserEntity author = userDomainService.getUserById(authorId);

            // 为每个接收者发送通知
            for (ContentNotificationService.NotificationRecipient recipient : recipients) {
                ContentUpdateNotificationData notificationData = new ContentUpdateNotificationData(
                        recipient.getUserId(),
                        recipient.getUserName(),
                        recipient.getUserEmail(),
                        recipient.getEmailNotificationEnabled(),
                        author.getName(),
                        post.getTitle(),
                        "文章",
                        post.getId()
                );

                notificationDomainService.sendNotification(notificationData);
            }
        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
            // 可以考虑使用日志记录或监控系统
        }
    }
}