package org.xhy.community.application.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.follow.entity.FollowEntity;
import org.xhy.community.domain.follow.service.FollowDomainService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ContentNotificationService.class);

    private final FollowDomainService followDomainService;
    private final UserDomainService userDomainService;

    public ContentNotificationService(FollowDomainService followDomainService,
                                      UserDomainService userDomainService) {
        this.followDomainService = followDomainService;
        this.userDomainService = userDomainService;
    }

    public List<NotificationRecipient> getContentFollowers(ContentType contentType, String contentId, String authorId) {
        Set<String> followerIds = new HashSet<>();

        IPage<FollowEntity> authorFollowers = followDomainService
                .getFollowersPaged(authorId, FollowTargetType.USER, 1, Integer.MAX_VALUE);
        int authorFollowerCount = authorFollowers.getRecords().size();
        followerIds.addAll(authorFollowers.getRecords().stream()
                .map(FollowEntity::getFollowerId)
                .collect(Collectors.toSet()));

        FollowTargetType followTargetType = mapContentTypeToFollowTargetType(contentType);
        int contentFollowerCount = 0;
        if (followTargetType != null) {
            IPage<FollowEntity> contentFollowers = followDomainService
                    .getFollowersPaged(contentId, followTargetType, 1, Integer.MAX_VALUE);
            contentFollowerCount = contentFollowers.getRecords().size();
            followerIds.addAll(contentFollowers.getRecords().stream()
                    .map(FollowEntity::getFollowerId)
                    .collect(Collectors.toSet()));
        }

        followerIds.remove(authorId);

        List<NotificationRecipient> recipients = followerIds.stream()
                .map(this::buildNotificationRecipient)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("[通知关注者] type={} contentId={} authorId={} 作者粉丝数={} 内容粉丝数={} 去重后接收者数={}",
                contentType, contentId, authorId, authorFollowerCount, contentFollowerCount, recipients.size());

        return recipients;
    }

    public List<NotificationRecipient> getSpecificContentFollowers(String targetContentId, FollowTargetType targetType, String authorId) {
        Set<String> followerIds = new HashSet<>();

        IPage<FollowEntity> authorFollowers = followDomainService
                .getFollowersPaged(authorId, FollowTargetType.USER, 1, Integer.MAX_VALUE);
        int authorFollowerCount = authorFollowers.getRecords().size();
        followerIds.addAll(authorFollowers.getRecords().stream()
                .map(FollowEntity::getFollowerId)
                .collect(Collectors.toSet()));

        IPage<FollowEntity> contentFollowers = followDomainService
                .getFollowersPaged(targetContentId, targetType, 1, Integer.MAX_VALUE);
        int contentFollowerCount = contentFollowers.getRecords().size();
        followerIds.addAll(contentFollowers.getRecords().stream()
                .map(FollowEntity::getFollowerId)
                .collect(Collectors.toSet()));

        followerIds.remove(authorId);

        List<NotificationRecipient> recipients = followerIds.stream()
                .map(this::buildNotificationRecipient)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("[通知关注者-特定目标] targetType={} targetContentId={} authorId={} 作者粉丝数={} 目标粉丝数={} 去重后接收者数={}",
                targetType, targetContentId, authorId, authorFollowerCount, contentFollowerCount, recipients.size());

        return recipients;
    }

    private NotificationRecipient buildNotificationRecipient(String userId) {
        try {
            UserEntity user = userDomainService.getUserById(userId);
            return new NotificationRecipient(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getEmailNotificationEnabled()
            );
        } catch (Exception e) {
            log.debug("[通知关注者] 构建接收者失败，userId={}，原因={}", userId, e.getMessage());
            return null;
        }
    }

    private FollowTargetType mapContentTypeToFollowTargetType(ContentType contentType) {
        return switch (contentType) {
            case POST -> FollowTargetType.POST;
            case COURSE -> FollowTargetType.COURSE;
            case CHAPTER -> FollowTargetType.CHAPTER;
            case COMMENT -> null;
            case UPDATE_LOG -> null;
            case INTERVIEW_QUESTION -> null;
            case PUBLISH_CONTENT -> null;
        };
    }

    public static class NotificationRecipient {
        private final String userId;
        private final String userName;
        private final String userEmail;
        private final Boolean emailNotificationEnabled;

        public NotificationRecipient(String userId, String userName, String userEmail, Boolean emailNotificationEnabled) {
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.emailNotificationEnabled = emailNotificationEnabled;
        }

        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public Boolean getEmailNotificationEnabled() { return emailNotificationEnabled; }
    }
}
