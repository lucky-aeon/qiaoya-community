package org.xhy.community.domain.chat.query;

public class ChatRoomQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String nameLike; // 可选：按名称模糊

    public ChatRoomQuery() {}

    public ChatRoomQuery(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum != null ? pageNum : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
    }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum != null ? pageNum : 1; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize != null ? pageSize : 10; }

    public String getNameLike() { return nameLike; }
    public void setNameLike(String nameLike) { this.nameLike = nameLike; }
}

