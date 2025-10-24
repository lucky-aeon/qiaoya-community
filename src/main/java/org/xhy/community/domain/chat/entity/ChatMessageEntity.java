package org.xhy.community.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.infrastructure.converter.UniversalListConverter;

import java.util.List;

@TableName("chat_messages")
public class ChatMessageEntity extends BaseEntity {
    private String roomId;
    private String senderId;
    private String content;
    private String quotedMessageId;

    @TableField(typeHandler = UniversalListConverter.class, jdbcType = JdbcType.OTHER)
    private List<String> mentionedUserIds;

    public ChatMessageEntity() {}

    public ChatMessageEntity(String roomId, String senderId, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
    }

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
}

