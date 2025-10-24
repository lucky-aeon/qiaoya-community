package org.xhy.community.application.chat.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageDTO {
    private String id;
    private String roomId;
    private String senderId;
    private String content;
    private String quotedMessageId;
    private List<String> mentionedUserIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 扩展信息：用于展示发送者资料
    private String senderName;
    private String senderAvatar;
    private java.util.List<String> senderTags;

    public ChatMessageDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getQuotedMessageId() { return quotedMessageId; }
    public void setQuotedMessageId(String quotedMessageId) { this.quotedMessageId = quotedMessageId; }

    public List<String> getMentionedUserIds() { return mentionedUserIds; }
    public void setMentionedUserIds(List<String> mentionedUserIds) { this.mentionedUserIds = mentionedUserIds; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public java.util.List<String> getSenderTags() { return senderTags; }
    public void setSenderTags(java.util.List<String> senderTags) { this.senderTags = senderTags; }
}
