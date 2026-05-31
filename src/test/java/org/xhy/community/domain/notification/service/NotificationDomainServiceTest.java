package org.xhy.community.domain.notification.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.context.ContentUpdateNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.notification.entity.NotificationEntity;
import org.xhy.community.domain.notification.repository.NotificationRepository;
import org.xhy.community.domain.notification.template.NotificationTemplate;
import org.xhy.community.domain.notification.template.NotificationTemplateRegistry;
import org.xhy.community.domain.notification.valueobject.ChannelType;
import org.xhy.community.domain.notification.valueobject.NotificationStatus;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.infrastructure.email.EmailService;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationDomainServiceTest {

    @Test
    void sendStoresFullContentForInAppButOnlyPlaceholderForEmail() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationTemplateRegistry templateRegistry = mock(NotificationTemplateRegistry.class);
        EmailService emailService = mock(EmailService.class);
        NotificationDomainService service = new NotificationDomainService(
                notificationRepository,
                templateRegistry,
                emailService
        );

        String inAppContent = "作者发布了新内容";
        String emailContent = "<html><body>完整邮件正文</body></html>";
        doReturn(new TestTemplate("站内标题", inAppContent))
                .when(templateRegistry).getTemplate(ContentType.POST, ChannelType.IN_APP);
        doReturn(new TestTemplate("邮件标题", emailContent))
                .when(templateRegistry).getTemplate(ContentType.POST, ChannelType.EMAIL);
        when(emailService.isEnabled()).thenReturn(true);
        when(emailService.sendEmail(eq(List.of("enabled@example.com")), eq("邮件标题"), eq(emailContent)))
                .thenReturn(true);

        ContentUpdateNotificationData notificationData = new ContentUpdateNotificationData(
                List.of(
                        new NotificationData.Recipient("user-enabled", "enabled@example.com", true),
                        new NotificationData.Recipient("user-disabled", "disabled@example.com", false),
                        new NotificationData.Recipient("user-empty-email", "", true)
                ),
                NotificationType.FOLLOWED_USER_POST,
                ContentType.POST,
                "作者",
                "内容标题",
                "post-1"
        );

        service.send(notificationData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<NotificationEntity>> insertCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(notificationRepository, times(2)).insert(insertCaptor.capture());
        verify(emailService).sendEmail(eq(List.of("enabled@example.com")), eq("邮件标题"), eq(emailContent));

        List<Collection<NotificationEntity>> insertedBatches = insertCaptor.getAllValues();
        List<NotificationEntity> inAppNotifications = insertedBatches.get(0).stream().toList();
        assertEquals(3, inAppNotifications.size());
        inAppNotifications.forEach(notification -> {
            assertEquals(ChannelType.IN_APP, notification.getChannelType());
            assertEquals(inAppContent, notification.getContent());
            assertEquals(NotificationStatus.SENT, notification.getStatus());
        });

        List<NotificationEntity> emailNotifications = insertedBatches.get(1).stream().toList();
        assertEquals(1, emailNotifications.size());
        NotificationEntity emailNotification = emailNotifications.get(0);
        assertEquals("user-enabled", emailNotification.getRecipientId());
        assertEquals(ChannelType.EMAIL, emailNotification.getChannelType());
        assertEquals(NotificationDomainService.EMAIL_CONTENT_PLACEHOLDER, emailNotification.getContent());
        assertEquals(NotificationStatus.SENT, emailNotification.getStatus());
    }

    @Test
    void sendMarksEmailNotificationFailedWhenEmailServiceFails() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationTemplateRegistry templateRegistry = mock(NotificationTemplateRegistry.class);
        EmailService emailService = mock(EmailService.class);
        NotificationDomainService service = new NotificationDomainService(
                notificationRepository,
                templateRegistry,
                emailService
        );

        doReturn(new TestTemplate("站内标题", "站内正文"))
                .when(templateRegistry).getTemplate(ContentType.POST, ChannelType.IN_APP);
        doReturn(new TestTemplate("邮件标题", "<html>邮件正文</html>"))
                .when(templateRegistry).getTemplate(ContentType.POST, ChannelType.EMAIL);
        when(emailService.isEnabled()).thenReturn(true);
        when(emailService.sendEmail(eq(List.of("enabled@example.com")), eq("邮件标题"), eq("<html>邮件正文</html>")))
                .thenReturn(false);

        ContentUpdateNotificationData notificationData = new ContentUpdateNotificationData(
                List.of(new NotificationData.Recipient("user-enabled", "enabled@example.com", true)),
                NotificationType.FOLLOWED_USER_POST,
                ContentType.POST,
                "作者",
                "内容标题",
                "post-1"
        );

        service.send(notificationData);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<NotificationEntity>> insertCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(notificationRepository, times(2)).insert(insertCaptor.capture());

        NotificationEntity emailNotification = insertCaptor.getAllValues().get(1).stream().toList().get(0);
        assertEquals(ChannelType.EMAIL, emailNotification.getChannelType());
        assertEquals(NotificationDomainService.EMAIL_CONTENT_PLACEHOLDER, emailNotification.getContent());
        assertEquals(NotificationStatus.FAILED, emailNotification.getStatus());
    }

    private static class TestTemplate implements NotificationTemplate<ContentUpdateNotificationData> {

        private final String title;
        private final String content;

        private TestTemplate(String title, String content) {
            this.title = title;
            this.content = content;
        }

        @Override
        public String renderTitle(ContentUpdateNotificationData data) {
            return title;
        }

        @Override
        public String renderContent(ContentUpdateNotificationData data) {
            return content;
        }

        @Override
        public Class<ContentUpdateNotificationData> getSupportedDataType() {
            return ContentUpdateNotificationData.class;
        }

        @Override
        public ContentType getContentType() {
            return ContentType.POST;
        }
    }
}
