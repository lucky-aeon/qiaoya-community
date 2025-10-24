package org.xhy.community.infrastructure.ws.model;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMessagePush {
    private String id;
    private String roomId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private java.util.List<String> senderTags;
    private String content;
    private String quotedMessageId;
    private List<String> mentionedUserIds;
    private LocalDateTime occurredAt;

    public ChatMessagePush() {}

    public ChatMessagePush(String id, String roomId, String senderId, String content,
                           String quotedMessageId, List<String> mentionedUserIds, LocalDateTime occurredAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.quotedMessageId = quotedMessageId;
        this.mentionedUserIds = mentionedUserIds;
        this.occurredAt = occurredAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public java.util.List<String> getSenderTags() { return senderTags; }
    public void setSenderTags(java.util.List<String> senderTags) { this.senderTags = senderTags; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getQuotedMessageId() { return quotedMessageId; }
    public void setQuotedMessageId(String quotedMessageId) { this.quotedMessageId = quotedMessageId; }
    public List<String> getMentionedUserIds() { return mentionedUserIds; }
    public void setMentionedUserIds(List<String> mentionedUserIds) { this.mentionedUserIds = mentionedUserIds; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
