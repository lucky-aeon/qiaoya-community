package org.xhy.community.infrastructure.ws.model;

import java.time.LocalDateTime;

public class ChatMentionPush {
    private String roomId;
    private String messageId;
    private String senderId;
    private String mentionedUserId;
    private String content;
    private String senderName;
    private String senderAvatar;
    private java.util.List<String> senderTags;
    private LocalDateTime occurredAt;

    public ChatMentionPush() {}

    public ChatMentionPush(String roomId, String messageId, String senderId, String mentionedUserId, String content, LocalDateTime occurredAt) {
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.mentionedUserId = mentionedUserId;
        this.content = content;
        this.occurredAt = occurredAt;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getMentionedUserId() { return mentionedUserId; }
    public void setMentionedUserId(String mentionedUserId) { this.mentionedUserId = mentionedUserId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public java.util.List<String> getSenderTags() { return senderTags; }
    public void setSenderTags(java.util.List<String> senderTags) { this.senderTags = senderTags; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}

