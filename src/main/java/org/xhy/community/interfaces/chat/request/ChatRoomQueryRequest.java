package org.xhy.community.interfaces.chat.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class ChatRoomQueryRequest extends PageRequest {
    private String nameLike;

    public ChatRoomQueryRequest() {}

    public ChatRoomQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getNameLike() { return nameLike; }
    public void setNameLike(String nameLike) { this.nameLike = nameLike; }
}

