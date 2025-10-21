package org.xhy.community.interfaces.favorite.request;

import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 我的收藏查询请求
 */
public class MyFavoritesQueryRequest extends PageRequest {

    /** 目标类型（可选，null表示查询所有类型） */
    private FavoriteTargetType targetType;

    public MyFavoritesQueryRequest() {
    }

    public FavoriteTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(FavoriteTargetType targetType) {
        this.targetType = targetType;
    }
}
