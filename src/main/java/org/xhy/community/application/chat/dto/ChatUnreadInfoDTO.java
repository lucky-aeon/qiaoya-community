package org.xhy.community.application.chat.dto;

public class ChatUnreadInfoDTO {
    private long count;
    private String firstUnreadId;
    private String firstUnreadOccurredAt; // ISO-8601 字符串

    public ChatUnreadInfoDTO() {}
    public ChatUnreadInfoDTO(long count, String firstUnreadId, String firstUnreadOccurredAt) {
        this.count = count;
        this.firstUnreadId = firstUnreadId;
        this.firstUnreadOccurredAt = firstUnreadOccurredAt;
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public String getFirstUnreadId() { return firstUnreadId; }
    public void setFirstUnreadId(String firstUnreadId) { this.firstUnreadId = firstUnreadId; }
    public String getFirstUnreadOccurredAt() { return firstUnreadOccurredAt; }
    public void setFirstUnreadOccurredAt(String firstUnreadOccurredAt) { this.firstUnreadOccurredAt = firstUnreadOccurredAt; }
}

