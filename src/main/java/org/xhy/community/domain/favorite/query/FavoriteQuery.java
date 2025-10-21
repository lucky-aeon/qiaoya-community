package org.xhy.community.domain.favorite.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

/**
 * 收藏查询对象
 */
public class FavoriteQuery extends BasePageQuery {

    /** 用户ID */
    private String userId;

    /** 目标类型 */
    private FavoriteTargetType targetType;

    public FavoriteQuery() {
    }

    public FavoriteQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public FavoriteTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(FavoriteTargetType targetType) {
        this.targetType = targetType;
    }
}
