package org.xhy.community.interfaces.like.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.xhy.community.domain.like.valueobject.LikeTargetType;

import java.util.List;

/**
 * 批量查询点赞状态/统计请求
 */
public class BatchLikeRequest {

    /** 目标列表 */
    @NotEmpty(message = "目标列表不能为空")
    @Valid
    private List<LikeTargetItem> targets;

    public BatchLikeRequest() {
    }

    public List<LikeTargetItem> getTargets() {
        return targets;
    }

    public void setTargets(List<LikeTargetItem> targets) {
        this.targets = targets;
    }

    /**
     * 点赞目标项
     */
    public static class LikeTargetItem {
        private String targetId;
        private LikeTargetType targetType;

        public LikeTargetItem() {
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
}