package org.xhy.community.domain.notification.template;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.*;

/**
 * 站内消息模板
 */
public class InAppNotificationTemplates {
    
    /**
     * 内容更新站内消息模板
     */
    public static class ContentUpdateTemplate implements NotificationTemplate<ContentUpdateNotificationData> {
        
        @Override
        public String renderTitle(ContentUpdateNotificationData data) {
            return "关注内容更新";
        }
        
        @Override
        public String renderContent(ContentUpdateNotificationData data) {
            String typeLabel = data.getContentType() == null ? "内容" : data.getContentType().getDescription();
            return data.getAuthorName() + " 发布了新的" + typeLabel + "：" + data.getContentTitle();
        }
        
        @Override
        public Class<ContentUpdateNotificationData> getSupportedDataType() {
            return ContentUpdateNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            // 关注内容更新（文章等）统一使用 PUBLISH_CONTENT 作为键
            return ContentType.PUBLISH_CONTENT;
        }
    }
    

    /**
     * 课程发布站内消息模板（复用内容更新渲染逻辑）
     */
    public static class CoursePublishedTemplate implements NotificationTemplate<ContentUpdateNotificationData> {

        @Override
        public String renderTitle(ContentUpdateNotificationData data) {
            return "关注内容更新";
        }

        @Override
        public String renderContent(ContentUpdateNotificationData data) {
            String typeLabel = data.getContentType() == null ? "内容" : data.getContentType().getDescription();
            return data.getAuthorName() + " 发布了新的" + typeLabel + "：" + data.getContentTitle();
        }

        @Override
        public Class<ContentUpdateNotificationData> getSupportedDataType() {
            return ContentUpdateNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            return ContentType.COURSE;
        }
    }

    /**
     * 面试题发布站内消息模板（复用内容更新渲染逻辑）
     */
    public static class InterviewQuestionPublishedTemplate implements NotificationTemplate<ContentUpdateNotificationData> {

        @Override
        public String renderTitle(ContentUpdateNotificationData data) {
            return "关注内容更新";
        }

        @Override
        public String renderContent(ContentUpdateNotificationData data) {
            String typeLabel = data.getContentType() == null ? "内容" : data.getContentType().getDescription();
            return data.getAuthorName() + " 发布了新的" + typeLabel + "：" + data.getContentTitle();
        }

        @Override
        public Class<ContentUpdateNotificationData> getSupportedDataType() {
            return ContentUpdateNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            return ContentType.INTERVIEW_QUESTION;
        }
    }

    /**
     * 评论站内消息模板
     */
    public static class CommentTemplate implements NotificationTemplate<CommentNotificationData> {
        
        @Override
        public String renderTitle(CommentNotificationData data) {
            return "新的评论";
        }
        
        @Override
        public String renderContent(CommentNotificationData data) {
            if (data.isReply()) {
                return data.getCommenterName() + " 回复了你的评论";
            } else {
                String targetLabel = data.getTargetType() == null ? "内容" : data.getTargetType().getDescription();
                return data.getCommenterName() + " 评论了你的" + targetLabel + "：" + data.getTargetTitle();
            }
        }
        
        @Override
        public Class<CommentNotificationData> getSupportedDataType() {
            return CommentNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            return ContentType.COMMENT;
        }
    }

    /**
     * 章节更新站内消息模板
     */
    public static class ChapterUpdatedTemplate implements NotificationTemplate<ChapterUpdatedNotificationData> {

        @Override
        public String renderTitle(ChapterUpdatedNotificationData data) {
            return "章节更新";
        }

        @Override
        public String renderContent(ChapterUpdatedNotificationData data) {
            return "课程《" + data.getCourseTitle() + "》新增章节：" + data.getChapterTitle();
        }

        @Override
        public Class<ChapterUpdatedNotificationData> getSupportedDataType() {
            return ChapterUpdatedNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            // 章节更新必须以 CHAPTER 作为模板键，避免与 POST 冲突
            return ContentType.CHAPTER;
        }
    }


    /**
     * 更新日志发布 - 站内消息模板
     */
    public static class UpdateLogPublishedTemplate implements NotificationTemplate<UpdateLogPublishedNotificationData> {

        @Override
        public String renderTitle(UpdateLogPublishedNotificationData data) {
            return "系统更新发布";
        }

        @Override
        public String renderContent(UpdateLogPublishedNotificationData data) {
            String title = data.getTitle() == null ? "" : data.getTitle();
            String base = (" 发布：" + title).trim();
            return base.isBlank() ? "更新日志发布" : base;
        }

        @Override
        public Class<UpdateLogPublishedNotificationData> getSupportedDataType() {
            return UpdateLogPublishedNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            return ContentType.UPDATE_LOG;
        }
    }

    /**
     * 聊天室 @ 提及 - 站内消息模板
     */
    public static class ChatMentionTemplate implements NotificationTemplate<ChatMentionNotificationData> {
        @Override
        public String renderTitle(ChatMentionNotificationData data) {
            return "你被提及";
        }

        @Override
        public String renderContent(ChatMentionNotificationData data) {
            String sender = data.getSenderName() == null ? "有人" : data.getSenderName();
            String room = (data.getRoomName() == null || data.getRoomName().isBlank()) ? "聊天室" : data.getRoomName();
            String preview = data.getContent();
            if (preview != null && preview.length() > 120) {
                preview = preview.substring(0, 120) + "...";
            }
            return sender + " 在《" + room + "》提及了你：" + (preview == null ? "" : preview);
        }

        @Override
        public Class<ChatMentionNotificationData> getSupportedDataType() {
            return ChatMentionNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            return ContentType.CHAT_MESSAGE;
        }
    }
}
