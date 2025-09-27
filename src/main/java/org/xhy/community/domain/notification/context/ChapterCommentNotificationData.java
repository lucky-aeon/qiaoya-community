package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.notification.valueobject.NotificationType;

/**
 * 章节被评论通知数据
 */
public class ChapterCommentNotificationData extends NotificationData {

    private final String commenterName;
    private final String courseId;
    private final String courseTitle;
    private final String chapterId;
    private final String chapterTitle;
    private final String commentContent;

    public ChapterCommentNotificationData(String recipientId, String recipientName, String recipientEmail,
                                          Boolean emailNotificationEnabled,
                                          String commenterName,
                                          String courseId, String courseTitle,
                                          String chapterId, String chapterTitle,
                                          String commentContent) {
        super(recipientId, recipientName, recipientEmail, emailNotificationEnabled, NotificationType.CHAPTER_COMMENT);
        this.commenterName = commenterName;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
        this.commentContent = commentContent;
    }

    public String getCommenterName() { return commenterName; }
    public String getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getChapterId() { return chapterId; }
    public String getChapterTitle() { return chapterTitle; }
    public String getCommentContent() { return commentContent; }
    public String getTruncatedCommentContent() {
        return commentContent != null && commentContent.length() > 100 ? commentContent.substring(0, 100) + "..." : commentContent;
    }

    public String getChapterPath() { return "/dashboard/courses/" + courseId + "/chapters/" + chapterId; }
}

