package org.xhy.community.domain.notification.query;

import org.xhy.community.domain.common.query.BasePageQuery;

/**
 * 通知查询对象
 */
public class NotificationQuery extends BasePageQuery {

    private String userId;

    public NotificationQuery() {
        super();
    }

    public NotificationQuery(String userId, Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}