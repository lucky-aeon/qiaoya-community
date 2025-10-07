package org.xhy.community.application.notification.handler;

import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.context.ChapterUpdatedNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 章节发布通知处理器
 * 处理章节发布时的通知逻辑和消息模板
 * 章节发布时需要通知关注课程和关注作者的用户
 */
@Component
public class ChapterNotificationHandler implements NotificationHandler {

    private final ChapterDomainService chapterDomainService;
    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    private final NotificationDomainService notificationDomainService;
    private final ContentNotificationService contentNotificationService;

    public ChapterNotificationHandler(ChapterDomainService chapterDomainService,
                                    CourseDomainService courseDomainService,
                                    UserDomainService userDomainService,
                                    NotificationDomainService notificationDomainService,
                                    ContentNotificationService contentNotificationService) {
        this.chapterDomainService = chapterDomainService;
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
        this.contentNotificationService = contentNotificationService;
    }

    @Override
    public ContentType getSupportedContentType() {
        return ContentType.CHAPTER;
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                 List<ContentNotificationService.NotificationRecipient> recipients) {
        try {
            // 获取章节和课程信息
            ChapterEntity chapter = chapterDomainService.getChapterById(contentId);
            CourseEntity course = courseDomainService.getCourseById(chapter.getCourseId());
            UserEntity author = userDomainService.getUserById(authorId);

            // 获取课程的关注者（包括关注课程的用户和关注作者的用户）
            List<ContentNotificationService.NotificationRecipient> courseFollowers =
                contentNotificationService.getSpecificContentFollowers(course.getId(), FollowTargetType.COURSE, authorId);

            // 为所有关注者发送课程更新通知

            ArrayList<NotificationData.Recipient> recipients1 = new ArrayList<>();
            for (ContentNotificationService.NotificationRecipient recipient : courseFollowers) {
                recipients1.add(new NotificationData.Recipient(recipient.getUserId(),recipient.getUserEmail(),recipient.getEmailNotificationEnabled()));
            }
            notificationDomainService.send(new ChapterUpdatedNotificationData(recipients1, NotificationType.CHAPTER_UPDATED,ContentType.CHAPTER,course.getId(),course.getTitle(),chapter.getId(),chapter.getTitle()));

        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
        }
    }
}
