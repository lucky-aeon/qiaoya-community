package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 章节被更新通知数据
 */
public class ChapterUpdatedNotificationData extends NotificationData {

    private final String courseId;
    private final String courseTitle;
    private final String chapterId;
    private final String chapterTitle;

    public ChapterUpdatedNotificationData(String recipientId, String recipientName, String recipientEmail,
                                          Boolean emailNotificationEnabled,
                                          String courseId, String courseTitle,
                                          String chapterId, String chapterTitle) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled, NotificationType.CHAPTER_UPDATED);
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

