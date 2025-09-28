package org.xhy.community.domain.notification.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.valueobject.ChannelType;
import org.xhy.community.domain.notification.context.*;
import org.xhy.community.infrastructure.config.EmailBrandingConfig;
import org.xhy.community.infrastructure.template.ClasspathTemplateLoader;
import org.xhy.community.infrastructure.template.SimpleTemplateRenderer;
import org.xhy.community.infrastructure.config.WebUrlConfig;
import org.xhy.community.infrastructure.util.ContentUrlResolver;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知模板注册器
 */
@Service
public class NotificationTemplateRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateRegistry.class);
    
    // 站内消息模板
    private final Map<Class<? extends NotificationData>, NotificationTemplate> inAppTemplates = new HashMap<>();
    
    // 站外消息模板
    private final Map<Class<? extends NotificationData>, NotificationTemplate> outAppTemplates = new HashMap<>();
    
    private final ClasspathTemplateLoader templateLoader;
    private final SimpleTemplateRenderer templateRenderer;
    private final EmailBrandingConfig brandingConfig;
    private final WebUrlConfig webUrlConfig;
    private final ContentUrlResolver contentUrlResolver;

    public NotificationTemplateRegistry(ClasspathTemplateLoader templateLoader,
                                        SimpleTemplateRenderer templateRenderer,
                                        EmailBrandingConfig brandingConfig,
                                        WebUrlConfig webUrlConfig,
                                        ContentUrlResolver contentUrlResolver) {
        this.templateLoader = templateLoader;
        this.templateRenderer = templateRenderer;
        this.brandingConfig = brandingConfig;
        this.webUrlConfig = webUrlConfig;
        this.contentUrlResolver = contentUrlResolver;
    }

    @PostConstruct
    public void initTemplates() {
        // 注册站内消息模板
        registerInAppTemplate(new InAppNotificationTemplates.NewFollowerTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.ContentUpdateTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.CDKActivatedTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.SubscriptionExpiredTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.CommentTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.ChapterUpdatedTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.ChapterCommentTemplate());

        // 注册站外消息模板
        registerFileBasedOutAppTemplates();
        
        log.info("通知模板注册完成: 站内模板{}个, 站外模板{}个", 
                inAppTemplates.size(), outAppTemplates.size());
    }

    private void registerFileBasedOutAppTemplates() {
        // 新关注者
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            NewFollowerNotificationData.class,
            "敲鸭社区 - 新的关注者",
            "new-follower.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("FOLLOWER_NAME", data.getFollowerName());
                m.put("FOLLOWER_PROFILE_URL", resolveUrl(data.getFollowerProfileUrl()));
                return m;
            },
            webUrlConfig
        ));

        // 关注内容更新
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            ContentUpdateNotificationData.class,
            "敲鸭社区 - 关注内容更新",
            "content-update.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("AUTHOR_NAME", data.getAuthorName());
                m.put("CONTENT_TYPE", data.getContentType() == null ? "" : data.getContentType().getDescription());
                m.put("CONTENT_TITLE", data.getContentTitle());
                m.put("CONTENT_URL", resolveUrl(contentUrlResolver.contentPath(data.getContentType(), data.getContentId())));
                return m;
            },
            webUrlConfig
        ));

        // CDK 激活
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            CDKActivatedNotificationData.class,
            "敲鸭社区 - CDK激活成功",
            "cdk-activated.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("CDK_CODE", data.getCdkCode());
                m.put("ACTIVATION_TIME", data.getActivationTime());
                return m;
            },
            webUrlConfig
        ));

        // 订阅即将过期
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            SubscriptionExpiredNotificationData.class,
            "敲鸭社区 - 订阅即将过期",
            "subscription-expiring.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("DAYS_REMAINING", String.valueOf(data.getDaysRemaining()));
                m.put("RENEWAL_URL", resolveUrl(data.getRenewalUrl()));
                return m;
            },
            webUrlConfig
        ));

        // 评论
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            CommentNotificationData.class,
            "敲鸭社区 - 新的评论",
            "comment.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("COMMENTER_NAME", data.getCommenterName());
                m.put("TARGET_TYPE", data.getTargetType() == null ? "" : data.getTargetType().getDescription());
                m.put("TARGET_TITLE", data.getTargetTitle());
                m.put("TRUNCATED_COMMENT", data.getTruncatedCommentContent());
                m.put("TARGET_URL", resolveUrl(contentUrlResolver.targetPath(data.getTargetType(), data.getTargetId())));
                return m;
            },
            webUrlConfig
        ));

        // 章节更新
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            ChapterUpdatedNotificationData.class,
            "敲鸭社区 - 章节更新",
            "chapter-updated.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("COURSE_TITLE", data.getCourseTitle());
                m.put("CHAPTER_TITLE", data.getChapterTitle());
                m.put("CHAPTER_URL", resolveUrl(data.getChapterPath()));
                return m;
            },
            webUrlConfig
        ));

        // 章节评论
        registerOutAppTemplate(new FileBasedNotificationTemplate<>(
            ChapterCommentNotificationData.class,
            "敲鸭社区 - 章节新评论",
            "chapter-comment.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("RECIPIENT_NAME", data.getRecipientName());
                m.put("COMMENTER_NAME", data.getCommenterName());
                m.put("COURSE_TITLE", data.getCourseTitle());
                m.put("CHAPTER_TITLE", data.getChapterTitle());
                m.put("TRUNCATED_COMMENT", data.getTruncatedCommentContent());
                m.put("CHAPTER_URL", resolveUrl(data.getChapterPath()));
                return m;
            },
            webUrlConfig
        ));
    }

    private String resolveUrl(String url) {
        if (url == null || url.isBlank()) return null;
        // 若为绝对地址，则取路径部分拼接新域名，确保域名一致
        String lower = url.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            int idx = url.indexOf("//");
            if (idx > 0) {
                int firstSlash = url.indexOf('/', idx + 2);
                String path = firstSlash > 0 ? url.substring(firstSlash) : "/";
                return webUrlConfig.resolve(path);
            }
        }
        // 相对路径
        return webUrlConfig.resolve(url);
    }

    // 内容路径构建改由 ContentUrlResolver 统一处理
    
    /**
     * 注册站内消息模板
     */
    private void registerInAppTemplate(NotificationTemplate template) {
        inAppTemplates.put(template.getSupportedDataType(), template);
    }
    
    /**
     * 注册站外消息模板
     */
    private void registerOutAppTemplate(NotificationTemplate template) {
        outAppTemplates.put(template.getSupportedDataType(), template);
    }
    
    /**
     * 获取站内消息模板
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getInAppTemplate(Class<T> dataType) {
        return inAppTemplates.get(dataType);
    }
    
    /**
     * 获取站外消息模板
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getOutAppTemplate(Class<T> dataType) {
        return outAppTemplates.get(dataType);
    }
    
    /**
     * 获取模板（根据渠道类型）
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getTemplate(Class<T> dataType, ChannelType channelType) {
        if (channelType == ChannelType.IN_APP) {
            return getInAppTemplate(dataType);
        } else {
            return getOutAppTemplate(dataType);
        }
    }
    
    /**
     * 检查是否支持指定类型和渠道的模板
     */
    public boolean hasTemplate(Class<? extends NotificationData> dataType, ChannelType channelType) {
        return getTemplate(dataType, channelType) != null;
    }
}
