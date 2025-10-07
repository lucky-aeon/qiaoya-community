package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.util.List;

/**
 * 章节被更新通知数据
 */
public class ChapterUpdatedNotificationData extends NotificationData {

    private final String courseId;
    private final String courseTitle;
    private final String chapterId;
    private final String chapterTitle;

    public ChapterUpdatedNotificationData(List<Recipient> recipients, NotificationType type, ContentType contentType, String courseId, String courseTitle, String chapterId, String chapterTitle) {
        super(recipients, type, contentType);
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
    }

    public String getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getChapterId() { return chapterId; }
    public String getChapterTitle() { return chapterTitle; }

    public String getChapterPath() {
        return "/dashboard/courses/" + courseId + "/chapters/" + chapterId;
    }
}

