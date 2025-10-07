package org.xhy.community.domain.notification.template;

import org.xhy.community.domain.notification.context.*;

/**
 * 站内消息模板
 */
public class InAppNotificationTemplates {
    
    /**
     * 新关注者站内消息模板
     */
    public static class NewFollowerTemplate implements NotificationTemplate<NewFollowerNotificationData> {
        
        @Override
        public String renderTitle(NewFollowerNotificationData data) {
            return "新的关注者";
        }
        
        @Override
        public String renderContent(NewFollowerNotificationData data) {
            return data.getFollowerName() + " 开始关注你了";
        }
        
        @Override
        public Class<NewFollowerNotificationData> getSupportedDataType() {
            return NewFollowerNotificationData.class;
        }
    }
    
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
    }
    
    /**
     * CDK激活站内消息模板
     */
    public static class CDKActivatedTemplate implements NotificationTemplate<CDKActivatedNotificationData> {
        
        @Override
        public String renderTitle(CDKActivatedNotificationData data) {
            return "CDK激活成功";
        }
        
        @Override
        public String renderContent(CDKActivatedNotificationData data) {
            return "你的CDK码 " + data.getCdkCode() + " 已成功激活";
        }
        
        @Override
        public Class<CDKActivatedNotificationData> getSupportedDataType() {
            return CDKActivatedNotificationData.class;
        }
    }
    
    /**
     * 订阅过期站内消息模板
     */
    public static class SubscriptionExpiredTemplate implements NotificationTemplate<SubscriptionExpiredNotificationData> {
        
        @Override
        public String renderTitle(SubscriptionExpiredNotificationData data) {
            return "订阅即将过期";
        }
        
        @Override
        public String renderContent(SubscriptionExpiredNotificationData data) {
            return "你的订阅将在 " + data.getDaysRemaining() + " 天后过期，请及时续费";
        }
        
        @Override
        public Class<SubscriptionExpiredNotificationData> getSupportedDataType() {
            return SubscriptionExpiredNotificationData.class;
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
            String targetLabel = data.getTargetType() == null ? "内容" : data.getTargetType().getDescription();
            return data.getCommenterName() + " 评论了你的" + targetLabel + "：" + data.getTargetTitle();
        }
        
        @Override
        public Class<CommentNotificationData> getSupportedDataType() {
            return CommentNotificationData.class;
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
    }

    /**
     * 章节评论站内消息模板
     */
    public static class ChapterCommentTemplate implements NotificationTemplate<ChapterCommentNotificationData> {

        @Override
        public String renderTitle(ChapterCommentNotificationData data) {
            return "章节新评论";
        }

        @Override
        public String renderContent(ChapterCommentNotificationData data) {
            return data.getCommenterName() + " 评论了你的课程《" + data.getCourseTitle() + "》中的章节《" + data.getChapterTitle() + "》";
        }

        @Override
        public Class<ChapterCommentNotificationData> getSupportedDataType() {
            return ChapterCommentNotificationData.class;
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
            String version = data.getVersion() == null ? "" : ("v" + data.getVersion());
            String title = data.getTitle() == null ? "" : data.getTitle();
            String base = (version + " 发布：" + title).trim();
            return base.isBlank() ? "更新日志发布" : base;
        }

        @Override
        public Class<UpdateLogPublishedNotificationData> getSupportedDataType() {
            return UpdateLogPublishedNotificationData.class;
        }
    }
}
