package org.xhy.community.interfaces.like.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.like.valueobject.LikeTargetType;

/**
 * 切换点赞请求
 * 用于点赞/取消点赞操作
 */
public class ToggleLikeRequest {

    /** 目标对象ID */
    @NotBlank(message = "目标ID不能为空")
    private String targetId;

    /** 目标类型：COURSE/POST/CHAPTER/COMMENT */
    @NotNull(message = "目标类型不能为空")
    private LikeTargetType targetType;

    public ToggleLikeRequest() {
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public LikeTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(LikeTargetType targetType) {
        this.targetType = targetType;
    }
}