package org.xhy.community.domain.notification.context;

import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.notification.valueobject.NotificationType;

import java.util.List;

/**
 * 聊天室 @ 提及 站内通知数据
 */
public class ChatMentionNotificationData extends NotificationData {
    private final String roomId;
    private final String messageId;
    private final String senderName;
    private final String content;
    private final String roomName;

    public ChatMentionNotificationData(List<Recipient> recipients,
                                       String roomId,
                                       String messageId,
                                       String senderName,
                                       String content,
                                       String roomName) {
        super(recipients, NotificationType.CHAT_MENTION, ContentType.CHAT_MESSAGE);
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderName = senderName;
        this.content = content;
        this.roomName = roomName;
    }

    public String getRoomId() { return roomId; }
    public String getMessageId() { return messageId; }
    public String getSenderName() { return senderName; }
    public String getContent() { return content; }
    public String getRoomName() { return roomName; }
}
