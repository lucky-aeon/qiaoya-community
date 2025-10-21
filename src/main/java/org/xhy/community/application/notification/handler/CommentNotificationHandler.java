package org.xhy.community.application.notification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.notification.context.CommentNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.util.List;

@Component
public class CommentNotificationHandler implements NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(CommentNotificationHandler.class);

    private final CommentDomainService commentDomainService;
    private final PostDomainService postDomainService;
    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    private final NotificationDomainService notificationDomainService;
    private final ChapterDomainService chapterDomainService;
    private final org.xhy.community.domain.interview.service.InterviewQuestionDomainService interviewQuestionDomainService;

    public CommentNotificationHandler(CommentDomainService commentDomainService,
                                      PostDomainService postDomainService,
                                      CourseDomainService courseDomainService,
                                      UserDomainService userDomainService,
                                      NotificationDomainService notificationDomainService, ChapterDomainService chapterDomainService,
                                      org.xhy.community.domain.interview.service.InterviewQuestionDomainService interviewQuestionDomainService) {
        this.commentDomainService = commentDomainService;
        this.postDomainService = postDomainService;
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
        this.chapterDomainService = chapterDomainService;
        this.interviewQuestionDomainService = interviewQuestionDomainService;
    }

    @Override
    public ContentType getSupportedContentType() {
        return ContentType.COMMENT;
    }

    /**
     * 评论通知不使用订阅者模型，而是直接通知被回复者和内容作者
     * 覆盖默认实现，返回空列表以避免无用的订阅者查询
     * 实际的接收者在 handleNotification 中根据业务逻辑确定
     */
    @Override
    public List<ContentNotificationService.NotificationRecipient> getRecipients(
            String contentId,
            String authorId,
            ContentNotificationService contentNotificationService) {
        // 评论通知有特殊逻辑（区分被回复者和内容作者，发送不同通知模板）
        // 不使用统一的订阅者列表，在 handleNotification 中自行处理
        return List.of();
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                   List<ContentNotificationService.NotificationRecipient> recipients) {
        try {
            CommentEntity comment = commentDomainService.getCommentById(contentId);
            UserEntity commenter = userDomainService.getUserById(authorId);

            String targetTitle = "";
            String targetAuthorId = "";
            FollowTargetType targetType = null;
            NotificationType notificationType = null;

            if (comment.getBusinessType() == BusinessType.POST) {
                PostEntity post = postDomainService.getPostById(comment.getBusinessId());
                UserEntity postAuthor = userDomainService.getUserById(post.getAuthorId());
                targetTitle = post.getTitle();
                targetAuthorId = post.getAuthorId();
                targetType = FollowTargetType.POST;
                notificationType = NotificationType.POST_COMMENT;
                log.info("[通知-评论] 目标=文章 postId={} authorId={}", post.getId(), postAuthor.getId());
            } else if (comment.getBusinessType() == BusinessType.COURSE) {
                CourseEntity course = courseDomainService.getCourseById(comment.getBusinessId());
                UserEntity courseAuthor = userDomainService.getUserById(course.getAuthorId());
                targetTitle = course.getTitle();
                targetAuthorId = course.getAuthorId();
                targetType = FollowTargetType.COURSE;
                notificationType = NotificationType.COURSE_COMMENT;
                log.info("[通知-评论] 目标=课程 courseId={} authorId={}", course.getId(), courseAuthor.getId());
            } else if (comment.getBusinessType() == BusinessType.CHAPTER) {
                ChapterEntity chapter = chapterDomainService.getChapterById(comment.getBusinessId());
                UserEntity courseAuthor = userDomainService.getUserById(chapter.getAuthorId());
                targetTitle = chapter.getTitle();
                targetAuthorId = chapter.getAuthorId();
                targetType = FollowTargetType.CHAPTER;
                notificationType = NotificationType.CHAPTER_UPDATED;
                log.info("[通知-评论] 目标=章节 chapterId={} authorId={}", chapter.getId(), courseAuthor.getId());
            } else if (comment.getBusinessType() == BusinessType.INTERVIEW_QUESTION) {
                org.xhy.community.domain.interview.entity.InterviewQuestionEntity question = interviewQuestionDomainService.getById(comment.getBusinessId());
                UserEntity questionAuthor = userDomainService.getUserById(question.getAuthorId());
                targetTitle = question.getTitle();
                targetAuthorId = question.getAuthorId();
                targetType = FollowTargetType.INTERVIEW_QUESTION;
                notificationType = NotificationType.INTERVIEW_QUESTION_COMMENT;
                log.info("[通知-评论] 目标=题目 questionId={} authorId={}", question.getId(), questionAuthor.getId());
            }

            boolean isReply = comment.getReplyUserId() != null;

            if (isReply && !authorId.equals(comment.getReplyUserId())) {
                UserEntity replyTarget = userDomainService.getUserById(comment.getReplyUserId());
                if (replyTarget != null) {
                    List<NotificationData.Recipient> recipientsA = List.of(
                            new NotificationData.Recipient(replyTarget.getId(), replyTarget.getEmail(), replyTarget.getEmailNotificationEnabled())
                    );
                    CommentNotificationData dataA = new CommentNotificationData(
                            recipientsA,
                            notificationType,
                            ContentType.COMMENT,
                            commenter.getName(),
                            targetTitle,
                            targetType,
                            comment.getContent(),
                            comment.getBusinessId(),
                            true
                    );
                    notificationDomainService.send(dataA);
                    log.info("[通知-评论] 已发送回复提醒，commentId={} replyToUser={}", comment.getId(), replyTarget.getId());
                }
            }

            if (!authorId.equals(targetAuthorId)) {
                UserEntity targetAuthor = userDomainService.getUserById(targetAuthorId);
                if (targetAuthor != null && (comment.getReplyUserId() == null || !targetAuthor.getId().equals(comment.getReplyUserId()))) {
                    List<NotificationData.Recipient> recipientsB = List.of(
                            new NotificationData.Recipient(targetAuthor.getId(), targetAuthor.getEmail(), targetAuthor.getEmailNotificationEnabled())
                    );
                    CommentNotificationData dataB = new CommentNotificationData(
                            recipientsB,
                            notificationType,
                            ContentType.COMMENT,
                            commenter.getName(),
                            targetTitle,
                            targetType,
                            comment.getContent(),
                            comment.getBusinessId(),
                            false
                    );
                    notificationDomainService.send(dataB);
                    log.info("[通知-评论] 已通知内容作者，commentId={} targetAuthor={}", comment.getId(), targetAuthor.getId());
                }
            }
        } catch (Exception e) {
            log.error("[通知-评论] 发送失败，commentId={} authorId={}，错误={}", contentId, authorId, e.getMessage(), e);
        }
    }
}
