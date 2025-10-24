package org.xhy.community.interfaces.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SendMessageRequest {
    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String quotedMessageId;

    private List<String> mentionedUserIds;

    // 幂等键（可选，但推荐）：同 senderId+roomId 组合唯一
    @Size(max = 64, message = "clientMessageId 最长64字符")
    private String clientMessageId;

    public SendMessageRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getQuotedMessageId() { return quotedMessageId; }
    public void setQuotedMessageId(String quotedMessageId) { this.quotedMessageId = quotedMessageId; }

    public List<String> getMentionedUserIds() { return mentionedUserIds; }
    public void setMentionedUserIds(List<String> mentionedUserIds) { this.mentionedUserIds = mentionedUserIds; }

    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }
}

