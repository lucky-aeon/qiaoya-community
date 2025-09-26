package org.xhy.community.domain.notification.template;

import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.infrastructure.config.EmailBrandingConfig;
import org.xhy.community.infrastructure.template.ClasspathTemplateLoader;
import org.xhy.community.infrastructure.template.SimpleTemplateRenderer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 基于文件的通知模板：从 classpath 加载 HTML，并用占位符渲染
 */
public class FileBasedNotificationTemplate<T extends NotificationData> implements NotificationTemplate<T> {

    private final Class<T> dataType;
    private final String title;
    private final String templateFileName;
    private final ClasspathTemplateLoader loader;
    private final SimpleTemplateRenderer renderer;
    private final EmailBrandingConfig brandingConfig;
    private final Function<T, Map<String, String>> contextProvider;

    public FileBasedNotificationTemplate(Class<T> dataType,
                                         String title,
                                         String templateFileName,
                                         ClasspathTemplateLoader loader,
                                         SimpleTemplateRenderer renderer,
                                         EmailBrandingConfig brandingConfig,
                                         Function<T, Map<String, String>> contextProvider) {
        this.dataType = dataType;
        this.title = title;
        this.templateFileName = templateFileName;
        this.loader = loader;
        this.renderer = renderer;
        this.brandingConfig = brandingConfig;
        this.contextProvider = contextProvider;
    }

    @Override
    public String renderTitle(T data) {
        return title;
    }

    @Override
    public String renderContent(T data) {
        String template = loader.load(templateFileName);
        Map<String, String> ctx = new HashMap<>();
        if (contextProvider != null) {
            Map<String, String> extra = contextProvider.apply(data);
            if (extra != null) ctx.putAll(extra);
        }
        // 通用占位符
        ctx.putIfAbsent("LOGO_SRC", brandingConfig.getLogoSrc() == null ? "" : brandingConfig.getLogoSrc());
        ctx.putIfAbsent("MANAGE_NOTIFICATIONS_URL", brandingConfig.getManageNotificationsUrl());
        return renderer.render(template, ctx);
    }

    @Override
    public Class<T> getSupportedDataType() {
        return dataType;
    }
}

