package org.xhy.community.domain.notification.template;

import org.xhy.community.domain.notification.context.*;

/**
 * 站外消息模板（保留：目前已切换为文件模板注册）
 * 说明：NotificationTemplateRegistry 已改为注册基于文件的模板。
 * 本类暂保留以避免破坏已有引用，但不会被新的注册器使用。
 */
public class OutAppNotificationTemplates {
    
    /**
     * 新关注者邮件模板
     */
    public static class NewFollowerTemplate implements NotificationTemplate<NewFollowerNotificationData> {
        
        @Override
        public String renderTitle(NewFollowerNotificationData data) {
            return "敲鸭社区 - 新的关注者";
        }
        
        @Override
        public String renderContent(NewFollowerNotificationData data) {
            return String.format(
                "Hi %s,<br><br>" +
                "%s 开始关注你了！<br><br>" +
                "点击查看个人主页：<a href=\"%s\">%s</a><br><br>" +
                "敲鸭社区团队",
                data.getRecipientName(),
                data.getFollowerName(),
                data.getFollowerProfileUrl(),
                data.getFollowerProfileUrl()
            );
        }
        
        @Override
        public Class<NewFollowerNotificationData> getSupportedDataType() {
            return NewFollowerNotificationData.class;
        }
    }
    
    /**
     * 内容更新邮件模板
     */
    public static class ContentUpdateTemplate implements NotificationTemplate<ContentUpdateNotificationData> {
        
        @Override
        public String renderTitle(ContentUpdateNotificationData data) {
            return "敲鸭社区 - 关注内容更新";
        }
        
        @Override
        public String renderContent(ContentUpdateNotificationData data) {
            return String.format(
                "Hi %s,<br><br>" +
                "%s 发布了新的%s：<br>" +
                "<strong>%s</strong><br><br>" +
                "点击查看：<a href=\"%s\">%s</a><br><br>" +
                "敲鸭社区团队",
                data.getRecipientName(),
                data.getAuthorName(),
                data.getContentType() == null ? "内容" : data.getContentType().getDescription(),
                data.getContentTitle(),
                data.getContentUrl(),
                data.getContentUrl()
            );
        }
        
        @Override
        public Class<ContentUpdateNotificationData> getSupportedDataType() {
            return ContentUpdateNotificationData.class;
        }
    }
    
    /**
     * CDK激活邮件模板
     */
    public static class CDKActivatedTemplate implements NotificationTemplate<CDKActivatedNotificationData> {
        
        @Override
        public String renderTitle(CDKActivatedNotificationData data) {
            return "敲鸭社区 - CDK激活成功";
        }
        
        @Override
        public String renderContent(CDKActivatedNotificationData data) {
            return String.format(
                "Hi %s,<br><br>" +
                "你的CDK码 <strong>%s</strong> 已成功激活！<br><br>" +
                "激活时间：%s<br><br>" +
                "感谢使用敲鸭社区！<br><br>" +
                "敲鸭社区团队",
                data.getRecipientName(),
                data.getCdkCode(),
                data.getActivationTime()
            );
        }
        
        @Override
        public Class<CDKActivatedNotificationData> getSupportedDataType() {
            return CDKActivatedNotificationData.class;
        }
    }
    
    /**
     * 订阅过期邮件模板
     */
    public static class SubscriptionExpiredTemplate implements NotificationTemplate<SubscriptionExpiredNotificationData> {
        
        @Override
        public String renderTitle(SubscriptionExpiredNotificationData data) {
            return "敲鸭社区 - 订阅即将过期";
        }
        
        @Override
        public String renderContent(SubscriptionExpiredNotificationData data) {
            return String.format(
                "Hi %s,<br><br>" +
                "你的订阅将在 <strong>%d</strong> 天后过期。<br><br>" +
                "为了不影响你的学习体验，请及时续费：<br>" +
                "<a href=\"%s\">立即续费</a><br><br>" +
                "如有疑问，请联系客服。<br><br>" +
                "敲鸭社区团队",
                data.getRecipientName(),
                data.getDaysRemaining(),
                data.getRenewalUrl()
            );
        }
        
        @Override
        public Class<SubscriptionExpiredNotificationData> getSupportedDataType() {
            return SubscriptionExpiredNotificationData.class;
        }
    }
    
    /**
     * 评论邮件模板
     */
    public static class CommentTemplate implements NotificationTemplate<CommentNotificationData> {
        
        @Override
        public String renderTitle(CommentNotificationData data) {
            return "敲鸭社区 - 新的评论";
        }
        
        @Override
        public String renderContent(CommentNotificationData data) {
            return String.format(
                "Hi %s,<br><br>" +
                "%s 评论了你的%s「%s」：<br><br>" +
                "<blockquote>%s</blockquote><br>" +
                "点击查看：<a href=\"%s\">%s</a><br><br>" +
                "敲鸭社区团队",
                data.getRecipientName(),
                data.getCommenterName(),
                data.getTargetType() == null ? "内容" : data.getTargetType().getDescription(),
                data.getTargetTitle(),
                data.getTruncatedCommentContent(),
                data.getTargetUrl(),
                data.getTargetUrl()
            );
        }
        
        @Override
        public Class<CommentNotificationData> getSupportedDataType() {
            return CommentNotificationData.class;
        }
    }
}
