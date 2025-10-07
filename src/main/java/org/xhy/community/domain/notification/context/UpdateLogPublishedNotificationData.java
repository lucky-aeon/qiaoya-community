package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 更新日志发布 - 通知数据
 */
public class UpdateLogPublishedNotificationData extends NotificationData {

    private final String version;       // 版本号
    private final String title;         // 更新标题
    private final String changelogPath; // 前端变更日志页面路径（相对路径）

    public UpdateLogPublishedNotificationData(String recipientId,
                                              String recipientName,
                                              String recipientEmail,
                                              Boolean emailNotificationEnabled,
                                              String version,
                                              String title,
                                              String changelogPath) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled, NotificationType.UPDATE_LOG_PUBLISHED);
        this.version = version;
        this.title = title;
        this.changelogPath = changelogPath;
    }

    public String getVersion() { return version; }
    public String getTitle() { return title; }
    public String getChangelogPath() { return changelogPath; }
}

