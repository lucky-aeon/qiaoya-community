package org.xhy.community.application.chat.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.notification.service.NotificationDomainService;
import org.xhy.community.domain.notification.context.ChatMentionNotificationData;
import org.xhy.community.domain.notification.context.NotificationData;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.chat.service.ChatRoomDomainService;
import org.xhy.community.domain.chat.entity.ChatRoomEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天相关的通知编排服务
 */
@Service
public class ChatNotificationAppService {

    private final NotificationDomainService notificationDomainService;
    private final UserDomainService userDomainService;
    private final ChatRoomDomainService chatRoomDomainService;

    public ChatNotificationAppService(NotificationDomainService notificationDomainService,
                                      UserDomainService userDomainService,
                                      ChatRoomDomainService chatRoomDomainService) {
        this.notificationDomainService = notificationDomainService;
        this.userDomainService = userDomainService;
        this.chatRoomDomainService = chatRoomDomainService;
    }

    /**
     * 向被 @ 的用户发送站内通知（不发送邮件）
     */
    public void sendChatMentionNotification(String recipientId, String roomId, String messageId,
                                            String senderId, String content) {
        if (recipientId == null || recipientId.isBlank()) return;

        // 构造收件人信息（如需将来发邮件，可补齐 email 与开关）
        List<NotificationData.Recipient> recipients = new ArrayList<>(1);
        try {
            UserEntity u = userDomainService.getUserById(recipientId);
            String email = u != null ? u.getEmail() : null;
            Boolean emailEnabled = u != null && Boolean.TRUE.equals(u.getEmailNotificationEnabled());
            recipients.add(new NotificationData.Recipient(recipientId, email, emailEnabled));
        } catch (Exception e) {
            // 降级：即使用户查询异常，也至少推送站内消息
            recipients.add(new NotificationData.Recipient(recipientId, null, false));
        }

        // 取发送者名称（用于模板）
        String senderName = null;
        try {
            UserEntity sender = userDomainService.getUserById(senderId);
            senderName = sender == null ? null : sender.getName();
        } catch (Exception ignored) {}

        String roomName = null;
        try {
            ChatRoomEntity room = chatRoomDomainService.getById(roomId);
            roomName = room == null ? null : room.getName();
        } catch (Exception ignored) {}

        ChatMentionNotificationData data = new ChatMentionNotificationData(
                recipients, roomId, messageId, senderName, content, roomName
        );
        notificationDomainService.send(data);
    }
}
