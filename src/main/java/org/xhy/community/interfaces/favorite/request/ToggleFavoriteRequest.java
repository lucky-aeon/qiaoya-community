package org.xhy.community.interfaces.favorite.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

/**
 * 切换收藏请求
 * 用于收藏/取消收藏操作
 */
public class ToggleFavoriteRequest {

    /** 目标对象ID */
    @NotBlank(message = "目标ID不能为空")
    private String targetId;

    /** 目标类型：POST/CHAPTER/COMMENT/INTERVIEW_QUESTION */
    @NotNull(message = "目标类型不能为空")
    private FavoriteTargetType targetType;

    public ToggleFavoriteRequest() {
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public FavoriteTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(FavoriteTargetType targetType) {
        this.targetType = targetType;
    }
}
