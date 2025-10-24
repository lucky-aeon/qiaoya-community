package org.xhy.community.domain.chat.event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 聊天消息创建事件（用于实时推送）
 */
public class ChatMessageCreatedEvent {
    private final String messageId;
    private final String roomId;
    private final String senderId;
    private final String content;
    private final String quotedMessageId;
    private final List<String> mentionedUserIds;
    private final LocalDateTime occurredAt;

    public ChatMessageCreatedEvent(String messageId, String roomId, String senderId, String content, String quotedMessageId, List<String> mentionedUserIds) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.quotedMessageId = quotedMessageId;
        this.mentionedUserIds = mentionedUserIds == null ? Collections.emptyList() : List.copyOf(mentionedUserIds);
        this.occurredAt = LocalDateTime.now();
    }

    public String getMessageId() { return messageId; }
    public String getRoomId() { return roomId; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getQuotedMessageId() { return quotedMessageId; }
    public List<String> getMentionedUserIds() { return mentionedUserIds; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
