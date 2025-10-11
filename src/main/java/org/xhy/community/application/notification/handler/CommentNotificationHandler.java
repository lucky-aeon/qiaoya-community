package org.xhy.community.application.notification.handler;

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

import java.util.Arrays;
import java.util.List;

/**
 * 评论创建通知处理器
 * 处理评论创建时的通知逻辑和消息模板
 * 评论创建时通知被评论内容的作者
 */
@Component
public class CommentNotificationHandler implements NotificationHandler {

    private final CommentDomainService commentDomainService;
    private final PostDomainService postDomainService;
    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    private final NotificationDomainService notificationDomainService;
    private final ChapterDomainService chapterDomainService;

    public CommentNotificationHandler(CommentDomainService commentDomainService,
                                      PostDomainService postDomainService,
                                      CourseDomainService courseDomainService,
                                      UserDomainService userDomainService,
                                      NotificationDomainService notificationDomainService, ChapterDomainService chapterDomainService) {
        this.commentDomainService = commentDomainService;
        this.postDomainService = postDomainService;
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
        this.chapterDomainService = chapterDomainService;
    }

    @Override
    public ContentType getSupportedContentType() {
        return ContentType.COMMENT;
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                 List<ContentNotificationService.NotificationRecipient> recipients) {
        // 评论通知不使用标准的关注者列表，而是通知被评论内容的作者
        // 这里的recipients参数在评论场景下不适用，我们需要直接处理

        try {
            // 获取评论信息
            CommentEntity comment = commentDomainService.getCommentById(contentId);
            UserEntity commenter = userDomainService.getUserById(authorId);

            String targetTitle = "";
            String targetAuthorId = "";
            String targetAuthorName = "";
            String targetAuthorEmail = "";
            FollowTargetType targetType = null;
            NotificationType notificationType = null;
            // 根据业务类型获取被评论内容信息
            if (comment.getBusinessType() == BusinessType.POST) {
                PostEntity post = postDomainService.getPostById(comment.getBusinessId());
                UserEntity postAuthor = userDomainService.getUserById(post.getAuthorId());

                targetTitle = post.getTitle();
                targetAuthorId = post.getAuthorId();
                targetAuthorName = postAuthor.getName();
                targetAuthorEmail = postAuthor.getEmail();
                targetType = FollowTargetType.POST;
                notificationType = NotificationType.POST_COMMENT;
            } else if (comment.getBusinessType() == BusinessType.COURSE) {
                CourseEntity course = courseDomainService.getCourseById(comment.getBusinessId());
                UserEntity courseAuthor = userDomainService.getUserById(course.getAuthorId());

                targetTitle = course.getTitle();
                targetAuthorId = course.getAuthorId();
                targetAuthorName = courseAuthor.getName();
                targetAuthorEmail = courseAuthor.getEmail();
                targetType = FollowTargetType.COURSE;
                notificationType = NotificationType.COURSE_COMMENT;
            }else if (comment.getBusinessType() == BusinessType.CHAPTER){
                ChapterEntity chapter = chapterDomainService.getChapterById(comment.getBusinessId());
                UserEntity courseAuthor = userDomainService.getUserById(chapter.getAuthorId());

                targetTitle = chapter.getTitle();
                targetAuthorId = chapter.getAuthorId();
                targetAuthorName = courseAuthor.getName();
                targetAuthorEmail = courseAuthor.getEmail();
                targetType = FollowTargetType.CHAPTER;
                notificationType = NotificationType.CHAPTER_UPDATED;
            }

            boolean isReply = comment.getReplyUserId() != null;

            // A) 如果是回复，给被回复用户单独发一条“回复评论”的通知
            if (isReply && !authorId.equals(comment.getReplyUserId())) {
                UserEntity replyTarget = userDomainService.getUserById(comment.getReplyUserId());
                if (replyTarget != null) {
                    java.util.List<NotificationData.Recipient> recipientsA = java.util.List.of(
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
                }
            }

            // B) 通知内容作者（若不是自己），内容仍按“评论了你的文章/课程/章节”展示
            if (!authorId.equals(targetAuthorId)) {
                UserEntity targetAuthor = userDomainService.getUserById(targetAuthorId);
                if (targetAuthor != null && (comment.getReplyUserId() == null || !targetAuthor.getId().equals(comment.getReplyUserId()))) {
                    java.util.List<NotificationData.Recipient> recipientsB = java.util.List.of(
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
                }
            }
        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
        }
    }
}
