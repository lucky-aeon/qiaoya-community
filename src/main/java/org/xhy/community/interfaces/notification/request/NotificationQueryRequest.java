package org.xhy.community.interfaces.notification.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 消息查询请求参数
 * 继承通用分页请求，支持消息列表的分页查询
 */
public class NotificationQueryRequest extends PageRequest {

    public NotificationQueryRequest() {
        super();
    }

    public NotificationQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
}