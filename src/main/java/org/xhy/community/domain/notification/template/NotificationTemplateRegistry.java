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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知模板注册器
 */
@Service
public class NotificationTemplateRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateRegistry.class);
    
    // 站内消息模板
    private final Map<ContentType, NotificationTemplate<?>> inAppTemplates = new HashMap<>();
    
    // 站外消息模板
    private final Map<ContentType, NotificationTemplate<?>> outAppTemplates = new HashMap<>();
    
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
        registerInAppTemplate(new InAppNotificationTemplates.ContentUpdateTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.CoursePublishedTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.InterviewQuestionPublishedTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.CommentTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.ChapterUpdatedTemplate());
        registerInAppTemplate(new InAppNotificationTemplates.UpdateLogPublishedTemplate());

        // 注册站外消息模板
        registerFileBasedOutAppTemplates();
        
        log.info("通知模板注册完成: 站内模板{}个, 站外模板{}个", 
                inAppTemplates.size(), outAppTemplates.size());
    }

    private void registerFileBasedOutAppTemplates() {


        // 关注内容更新
        FileBasedNotificationTemplate<ContentUpdateNotificationData> contentUpdateEmailTemplate = new FileBasedNotificationTemplate<>(
            ContentUpdateNotificationData.class,
            "敲鸭社区 - 关注内容更新",
            "content-update.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("AUTHOR_NAME", data.getAuthorName());
                m.put("CONTENT_TYPE", data.getContentType() == null ? "" : data.getContentType().getDescription());
                m.put("CONTENT_TITLE", data.getContentTitle());
                m.put("CONTENT_URL", resolveUrl(contentUrlResolver.contentPath(data.getContentType(), data.getContentId())));
                return m;
            },
            webUrlConfig
        );
        // 为不同内容类型复用同一份“关注内容更新”邮件模板
        registerOutAppTemplate(ContentType.PUBLISH_CONTENT, contentUpdateEmailTemplate);
        registerOutAppTemplate(ContentType.POST, contentUpdateEmailTemplate);
        registerOutAppTemplate(ContentType.COURSE, contentUpdateEmailTemplate);
        registerOutAppTemplate(ContentType.INTERVIEW_QUESTION, contentUpdateEmailTemplate);

        // 评论/回复评论（邮件）
        registerOutAppTemplate(ContentType.COMMENT,new FileBasedNotificationTemplate<>(
            CommentNotificationData.class,
            "敲鸭社区 - 新的评论",
            "comment.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                String commenter = data.getCommenterName() == null ? "" : data.getCommenterName();
                String targetType = data.getTargetType() == null ? "" : data.getTargetType().getDescription();
                String targetTitle = data.getTargetTitle() == null ? "" : data.getTargetTitle();

                // 预览行与动作行：根据是否为“回复评论”生成不同文案
                if (data.isReply()) {
                    m.put("PREVIEW_LINE", commenter + " 回复了你");
                    m.put("ACTION_LINE", commenter + " 回复了你的评论：");
                } else {
                    m.put("PREVIEW_LINE", commenter + " 评论了你");
                    m.put("ACTION_LINE", commenter + " 评论了你的 " + targetType + "《" + targetTitle + "》：");
                }

                m.put("COMMENTER_NAME", commenter);
                m.put("TARGET_TYPE", targetType);
                m.put("TARGET_TITLE", targetTitle);
                m.put("TRUNCATED_COMMENT", data.getTruncatedCommentContent());
                m.put("TARGET_URL", resolveUrl(contentUrlResolver.targetPath(data.getTargetType(), data.getTargetId())));
                return m;
            },
            webUrlConfig
        ));

        // 章节更新
        registerOutAppTemplate(ContentType.CHAPTER,new FileBasedNotificationTemplate<>(
            ChapterUpdatedNotificationData.class,
            "敲鸭社区 - 章节更新",
            "chapter-updated.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("COURSE_TITLE", data.getCourseTitle());
                m.put("CHAPTER_TITLE", data.getChapterTitle());
                m.put("CHAPTER_URL", resolveUrl(data.getChapterPath()));
                return m;
            },
            webUrlConfig
        ));


        // 更新日志发布
        registerOutAppTemplate(ContentType.UPDATE_LOG,new FileBasedNotificationTemplate<>(
            UpdateLogPublishedNotificationData.class,
            "敲鸭社区 - 更新日志发布",
            "update-log-published.html",
            templateLoader,
            templateRenderer,
            brandingConfig,
            data -> {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("TITLE", data.getTitle() == null ? "" : data.getTitle());
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
    private void registerInAppTemplate(NotificationTemplate<?> template) {
        ContentType key = template.getContentType();
        NotificationTemplate<?> old = inAppTemplates.put(key, template);
        if (old != null && old.getClass() != template.getClass()) {
            log.warn("站内模板键冲突: key={}, old={}, new={}", key, old.getClass().getSimpleName(), template.getClass().getSimpleName());
        }
    }
    
    /**
     * 注册站外消息模板
     */
    private void registerOutAppTemplate(ContentType contentType, NotificationTemplate<?> template) {
        NotificationTemplate<?> old = outAppTemplates.put(contentType, template);
        if (old != null && old.getClass() != template.getClass()) {
            log.warn("站外模板键冲突: key={}, old={}, new={}", contentType, old.getClass().getSimpleName(), template.getClass().getSimpleName());
        }
    }
    
    /**
     * 获取站内消息模板
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getInAppTemplate(ContentType contentType) {
        return (NotificationTemplate<T>) inAppTemplates.get(contentType);
    }
    
    /**
     * 获取站外消息模板
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getOutAppTemplate(ContentType contentType) {
        return (NotificationTemplate<T>) outAppTemplates.get(contentType);
    }
    
    /**
     * 获取模板（根据渠道类型）
     */
    @SuppressWarnings("unchecked")
    public <T extends NotificationData> NotificationTemplate<T> getTemplate(ContentType contentType, ChannelType channelType) {
        if (channelType == ChannelType.IN_APP) {
            return getInAppTemplate(contentType);
        } else {
            return getOutAppTemplate(contentType);
        }
    }
    
    /**
     * 检查是否支持指定类型和渠道的模板
     */
    public boolean hasTemplate(ContentType contentType, ChannelType channelType) {
        return getTemplate(contentType, channelType) != null;
    }
}
