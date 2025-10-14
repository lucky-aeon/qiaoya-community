package org.xhy.community.application.notification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.ArrayList;
import java.util.List;

@Component
public class CourseNotificationHandler implements NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(CourseNotificationHandler.class);

    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    private final NotificationDomainService notificationDomainService;

    public CourseNotificationHandler(CourseDomainService courseDomainService,
                                     UserDomainService userDomainService,
                                     NotificationDomainService notificationDomainService) {
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
    }

    @Override
    public ContentType getSupportedContentType() {
        return ContentType.COURSE;
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                   List<ContentNotificationService.NotificationRecipient> recipients) {
        try {
            CourseEntity course = courseDomainService.getCourseById(contentId);
            UserEntity author = userDomainService.getUserById(authorId);

            log.info("[通知-课程] 准备发送，courseId={} authorId={} recipients={}", course.getId(), authorId, recipients.size());

            List<NotificationData.Recipient> recipients2 = new ArrayList<>();
            for (ContentNotificationService.NotificationRecipient recipient : recipients) {
                recipients2.add(new NotificationData.Recipient(recipient.getUserId(), recipient.getUserEmail(), recipient.getEmailNotificationEnabled()));
            }

            ContentUpdateNotificationData notificationData = new ContentUpdateNotificationData(
                    recipients2,
                    NotificationType.CHAPTER_UPDATED,
                    ContentType.COURSE,
                    author.getName(),
                    course.getTitle(),
                    course.getId()
            );

            notificationDomainService.send(notificationData);
            log.info("[通知-课程] 已发送，courseId={} recipients={}", course.getId(), recipients.size());
        } catch (Exception e) {
            log.error("[通知-课程] 发送失败，contentId={} authorId={}，错误={}", contentId, authorId, e.getMessage(), e);
        }
    }
}
