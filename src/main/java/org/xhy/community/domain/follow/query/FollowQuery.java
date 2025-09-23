package org.xhy.community.domain.follow.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

public class FollowQuery extends BasePageQuery {

    private String followerId;
    private FollowTargetType targetType;

    public FollowQuery() {}

    public FollowQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public FollowTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(FollowTargetType targetType) {
        this.targetType = targetType;
    }
}

