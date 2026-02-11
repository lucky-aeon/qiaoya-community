package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.util.List;

/**
 * 更新日志发布 - 通知数据
 */
public class UpdateLogPublishedNotificationData extends NotificationData {

    private final String title;         // 更新标题
    private final String updateLogId;   // 更新日志ID

    public UpdateLogPublishedNotificationData(List<Recipient> recipients, NotificationType type, ContentType contentType, String title, String updateLogId) {
        super(recipients, type, contentType);
        this.title = title;
        this.updateLogId = updateLogId;
    }

    public String getTitle() { return title; }
    public String getUpdateLogId() { return updateLogId; }
}
