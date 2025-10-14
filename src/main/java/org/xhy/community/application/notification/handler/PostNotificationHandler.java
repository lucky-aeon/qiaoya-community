package org.xhy.community.application.notification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostNotificationHandler implements NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(PostNotificationHandler.class);

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
            PostEntity post = postDomainService.getPostById(contentId);
            UserEntity author = userDomainService.getUserById(authorId);

            log.info("[通知-文章] 准备发送，postId={} authorId={} recipients={}", post.getId(), authorId, recipients.size());

            List<NotificationData.Recipient> recipients2 = new ArrayList<>();
            for (ContentNotificationService.NotificationRecipient recipient : recipients) {
                recipients2.add(new NotificationData.Recipient(recipient.getUserId(), recipient.getUserEmail(), recipient.getEmailNotificationEnabled()));
            }

            ContentUpdateNotificationData contentUpdateNotificationData = new ContentUpdateNotificationData(
                    recipients2,
                    NotificationType.FOLLOWED_USER_POST,
                    ContentType.PUBLISH_CONTENT,
                    author.getName(),
                    post.getTitle(),
                    post.getId()
            );
            notificationDomainService.send(contentUpdateNotificationData);

            log.info("[通知-文章] 已发送，postId={} recipients={}", post.getId(), recipients.size());
        } catch (Exception e) {
            log.error("[通知-文章] 发送失败，contentId={} authorId={}，错误={}", contentId, authorId, e.getMessage(), e);
        }
    }
}
