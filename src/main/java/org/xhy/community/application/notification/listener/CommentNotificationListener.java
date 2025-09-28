package org.xhy.community.application.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.comment.event.CommentCreatedEvent;
import org.xhy.community.domain.notification.context.CommentNotificationData;
import org.xhy.community.domain.notification.context.ChapterCommentNotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.user.service.UserDomainService;

/**
 * 评论相关事件监听器 - Application层负责流程编排
 * 位置：org.xhy.community.application.notification.listener.CommentNotificationListener
 */
@Component
public class CommentNotificationListener {
    
    private static final Logger log = LoggerFactory.getLogger(CommentNotificationListener.class);
    
    private final NotificationDomainService notificationDomainService;
    private final UserDomainService userDomainService;
    private final ChapterDomainService chapterDomainService;
    private final CourseDomainService courseDomainService;

    public CommentNotificationListener(NotificationDomainService notificationDomainService,
                                     UserDomainService userDomainService,
                                     ChapterDomainService chapterDomainService,
                                     CourseDomainService courseDomainService) {
        this.notificationDomainService = notificationDomainService;
        this.userDomainService = userDomainService;
        this.chapterDomainService = chapterDomainService;
        this.courseDomainService = courseDomainService;
    }
    
    /**
     * 监听评论创建事件 - Application层流程编排
     */
    @EventListener
    @Async
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        try {
            // Application层流程编排：业务规则判断
            // 不给自己发通知
            if (event.getCommenterId().equals(event.getTargetAuthorId())) {
                return;
            }
            
            // Application层流程编排：查询目标作者的邮箱开关设置
            Boolean emailNotificationEnabled = userDomainService.getUserById(event.getTargetAuthorId())
                .getEmailNotificationEnabled();

            if (event.getTargetType() == FollowTargetType.CHAPTER) {
                // 章节评论：构建章节专用通知，带有章节路径
                ChapterEntity chapter = chapterDomainService.getChapterById(event.getTargetId());
                CourseEntity course = courseDomainService.getCourseById(chapter.getCourseId());

                ChapterCommentNotificationData notificationData = new ChapterCommentNotificationData(
                        event.getTargetAuthorId(),
                        event.getTargetAuthorName(),
                        event.getTargetAuthorEmail(),
                        emailNotificationEnabled,
                        event.getCommenterName(),
                        course.getId(), course.getTitle(),
                        chapter.getId(), chapter.getTitle(),
                        event.getCommentContent()
                );
                notificationDomainService.sendNotification(notificationData);
            } else {
                // 文章/课程评论：使用通用评论通知
                CommentNotificationData notificationData =
                        new CommentNotificationData(
                                event.getTargetAuthorId(),
                                event.getTargetAuthorName(),
                                event.getTargetAuthorEmail(),
                                emailNotificationEnabled,
                                event.getCommenterName(),
                                event.getTargetTitle(),
                                event.getTargetType(),
                                event.getCommentContent(),
                                event.getTargetId()
                        );
                notificationDomainService.sendNotification(notificationData);
            }
            
        } catch (Exception e) {
            log.error("处理评论事件失败", e);
        }
    }
}
