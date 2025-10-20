package org.xhy.community.application.notification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhy.community.application.notification.service.ContentNotificationService;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.service.InterviewQuestionDomainService;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.ArrayList;
import java.util.List;

/**
 * 面试题发布通知处理器
 */
@Component
public class InterviewQuestionNotificationHandler implements NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(InterviewQuestionNotificationHandler.class);

    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final UserDomainService userDomainService;
    private final NotificationDomainService notificationDomainService;

    public InterviewQuestionNotificationHandler(InterviewQuestionDomainService interviewQuestionDomainService,
                                                UserDomainService userDomainService,
                                                NotificationDomainService notificationDomainService) {
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
    }

    @Override
    public ContentType getSupportedContentType() {
        return ContentType.INTERVIEW_QUESTION;
    }

    @Override
    public void handleNotification(String contentId, String authorId,
                                   List<ContentNotificationService.NotificationRecipient> recipients) {
        try {
            InterviewQuestionEntity question = interviewQuestionDomainService.getById(contentId);
            UserEntity author = userDomainService.getUserById(authorId);

            log.info("[通知-题目] 准备发送，questionId={} authorId={} recipients={}", question.getId(), authorId, recipients.size());

            List<NotificationData.Recipient> receivers = new ArrayList<>();
            for (ContentNotificationService.NotificationRecipient r : recipients) {
                receivers.add(new NotificationData.Recipient(r.getUserId(), r.getUserEmail(), r.getEmailNotificationEnabled()));
            }

            ContentUpdateNotificationData data = new ContentUpdateNotificationData(
                    receivers,
                    NotificationType.FOLLOWED_USER_POST, // 复用“关注用户发布新内容”
                    ContentType.INTERVIEW_QUESTION,
                    author.getName(),
                    question.getTitle(),
                    question.getId()
            );
            notificationDomainService.send(data);

            log.info("[通知-题目] 已发送，questionId={} recipients={}", question.getId(), recipients.size());
        } catch (Exception e) {
            log.error("[通知-题目] 发送失败，questionId={} authorId={}，错误={}", contentId, authorId, e.getMessage(), e);
        }
    }
}

