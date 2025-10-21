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

    /**
     * 章节发布通知课程订阅者，而不是章节订阅者
     * 覆盖默认实现，返回课程订阅者列表
     */
    @Override
    public List<ContentNotificationService.NotificationRecipient> getRecipients(
            String contentId,
            String authorId,
            ContentNotificationService contentNotificationService) {
        try {
            // 获取章节所属的课程ID
            ChapterEntity chapter = chapterDomainService.getChapterById(contentId);
            String courseId = chapter.getCourseId();

            // 返回课程订阅者（关注作者 + 关注课程）
            return contentNotificationService.getSpecificContentFollowers(
                    courseId,
                    FollowTargetType.COURSE,
                    authorId
            );
        } catch (Exception e) {
            log.error("[通知-章节] 获取接收者失败，chapterId={}，错误={}", contentId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                   List<ContentNotificationService.NotificationRecipient> recipients) {
        try {
            ChapterEntity chapter = chapterDomainService.getChapterById(contentId);
            CourseEntity course = courseDomainService.getCourseById(chapter.getCourseId());
            UserEntity author = userDomainService.getUserById(authorId);

            log.info("[通知-章节] 准备发送，courseId={} chapterId={} authorId={} recipients={}",
                    course.getId(), chapter.getId(), authorId, recipients.size());

            ArrayList<NotificationData.Recipient> recipients1 = new ArrayList<>();
            for (ContentNotificationService.NotificationRecipient recipient : recipients) {
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

            log.info("[通知-章节] 已发送，courseId={} chapterId={} recipients={}", course.getId(), chapter.getId(), recipients.size());
        } catch (Exception e) {
            log.error("[通知-章节] 发送失败，contentId={} authorId={}，错误={}", contentId, authorId, e.getMessage(), e);
        }
    }
}
