package org.xhy.community.interfaces.chat.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 聊天消息分页请求（简化版：仅页码与每页大小）。
 */
public class RoomMessageQueryRequest extends PageRequest {
    public RoomMessageQueryRequest() {}
    public RoomMessageQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
}
