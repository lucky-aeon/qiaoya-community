package org.xhy.community.application.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.follow.service.FollowDomainService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.follow.entity.FollowEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容通知服务
 * 统一处理关注者查询和用户信息获取逻辑
 */
@Service
public class ContentNotificationService {

    private final FollowDomainService followDomainService;
    private final UserDomainService userDomainService;

    public ContentNotificationService(FollowDomainService followDomainService,
                                    UserDomainService userDomainService) {
        this.followDomainService = followDomainService;
        this.userDomainService = userDomainService;
    }

    /**
     * 获取内容发布的关注者信息
     * 根据内容类型决定查询策略
     */
    public List<NotificationRecipient> getContentFollowers(ContentType contentType, String contentId, String authorId) {
        Set<String> followerIds = new HashSet<>();

        // 获取关注作者的用户
        IPage<FollowEntity> authorFollowers = followDomainService
                .getFollowersPaged(authorId, FollowTargetType.USER, 1, Integer.MAX_VALUE);
        followerIds.addAll(authorFollowers.getRecords().stream()
                .map(FollowEntity::getFollowerId)
                .collect(Collectors.toSet()));

        // 根据内容类型获取关注内容本身的用户
        FollowTargetType followTargetType = mapContentTypeToFollowTargetType(contentType);
        if (followTargetType != null) {
            IPage<FollowEntity> contentFollowers = followDomainService
                    .getFollowersPaged(contentId, followTargetType, 1, Integer.MAX_VALUE);
            followerIds.addAll(contentFollowers.getRecords().stream()
                    .map(FollowEntity::getFollowerId)
                    .collect(Collectors.toSet()));
        }

        // 移除作者自己（避免自己给自己发通知）
        followerIds.remove(authorId);

        // 批量获取用户信息并构建通知接收者列表
        return followerIds.stream()
                .map(this::buildNotificationRecipient)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取特定类型关注的关注者信息（如章节更新需要查询课程关注者）
     */
    public List<NotificationRecipient> getSpecificContentFollowers(String targetContentId, FollowTargetType targetType, String authorId) {
        Set<String> followerIds = new HashSet<>();

        // 获取关注作者的用户
        IPage<FollowEntity> authorFollowers = followDomainService
                .getFollowersPaged(authorId, FollowTargetType.USER, 1, Integer.MAX_VALUE);
        followerIds.addAll(authorFollowers.getRecords().stream()
                .map(FollowEntity::getFollowerId)
                .collect(Collectors.toSet()));

        // 获取关注目标内容的用户
        IPage<FollowEntity> contentFollowers = followDomainService
                .getFollowersPaged(targetContentId, targetType, 1, Integer.MAX_VALUE);
        followerIds.addAll(contentFollowers.getRecords().stream()
                .map(FollowEntity::getFollowerId)
                .collect(Collectors.toSet()));

        // 移除作者自己
        followerIds.remove(authorId);

        return followerIds.stream()
                .map(this::buildNotificationRecipient)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 构建通知接收者信息
     */
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
            // 用户不存在或查询失败，跳过该用户
            return null;
        }
    }

    /**
     * 将ContentType映射到FollowTargetType
     */
    private FollowTargetType mapContentTypeToFollowTargetType(ContentType contentType) {
        return switch (contentType) {
            case POST -> FollowTargetType.POST;
            case COURSE -> FollowTargetType.COURSE;
            case CHAPTER -> FollowTargetType.CHAPTER;
            case COMMENT -> null; // 评论本身不支持关注
            case UPDATE_LOG -> null;
            case PUBLISH_CONTENT -> null;
        };
    }

    /**
     * 通知接收者信息
     */
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