package org.xhy.community.application.notification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.notification.context.ChapterUpdatedNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChapterNotificationHandler implements NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(ChapterNotificationHandler.class);

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
            ChapterEntity chapter = chapterDomainService.getChapterById(contentId);
            CourseEntity course = courseDomainService.getCourseById(chapter.getCourseId());
            UserEntity author = userDomainService.getUserById(authorId);

            List<ContentNotificationService.NotificationRecipient> courseFollowers =
                contentNotificationService.getSpecificContentFollowers(course.getId(), FollowTargetType.COURSE, authorId);

            log.info("[通知-章节] 准备发送，courseId={} chapterId={} authorId={} recipients={}",
                    course.getId(), chapter.getId(), authorId, courseFollowers.size());

            ArrayList<NotificationData.Recipient> recipients1 = new ArrayList<>();
            for (ContentNotificationService.NotificationRecipient recipient : courseFollowers) {
                recipients1.add(new NotificationData.Recipient(recipient.getUserId(), recipient.getUserEmail(), recipient.getEmailNotificationEnabled()));
            }
            notificationDomainService.send(new ChapterUpdatedNotificationData(
                    recipients1,
                    NotificationType.CHAPTER_UPDATED,
                    ContentType.CHAPTER,
                    course.getId(),
                    course.getTitle(),
                    chapter.getId(),
                    chapter.getTitle()
            ));

            log.info("[通知-章节] 已发送，courseId={} chapterId={} recipients={}", course.getId(), chapter.getId(), courseFollowers.size());
        } catch (Exception e) {
            log.error("[通知-章节] 发送失败，contentId={} authorId={}，错误={}", contentId, authorId, e.getMessage(), e);
        }
    }
}
