package org.xhy.community.application.notification.handler;

import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.user.entity.UserEntity;

import java.util.List;

/**
 * 课程发布通知处理器
 * 处理课程发布时的通知逻辑和消息模板
 */
@Component
public class CourseNotificationHandler implements NotificationHandler {

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
            // 获取课程信息
            CourseEntity course = courseDomainService.getCourseById(contentId);
            UserEntity author = userDomainService.getUserById(authorId);

            // 为每个接收者发送通知
            for (ContentNotificationService.NotificationRecipient recipient : recipients) {
                ContentUpdateNotificationData notificationData = new ContentUpdateNotificationData(
                        recipient.getUserId(),
                        recipient.getUserName(),
                        recipient.getUserEmail(),
                        recipient.getEmailNotificationEnabled(),
                        author.getName(),
                        course.getTitle(),
                        ContentType.COURSE,
                        course.getId()
                );

                notificationDomainService.sendNotification(notificationData);
            }
        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
        }
    }
}
