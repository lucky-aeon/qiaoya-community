package org.xhy.community.domain.chat.query;

public class ChatMessageQuery {
    private String roomId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public ChatMessageQuery() {}

    public ChatMessageQuery(String roomId, Integer pageNum, Integer pageSize) {
        this.roomId = roomId;
        this.pageNum = pageNum != null ? pageNum : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum != null ? pageNum : 1; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize != null ? pageSize : 10; }
}
