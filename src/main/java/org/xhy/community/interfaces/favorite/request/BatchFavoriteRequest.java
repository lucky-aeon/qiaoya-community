package org.xhy.community.interfaces.favorite.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

import java.util.List;

/**
 * 批量查询收藏状态请求
 */
public class BatchFavoriteRequest {

    /** 目标列表 */
    @NotEmpty(message = "目标列表不能为空")
    @Valid
    private List<FavoriteTargetItem> targets;

    public BatchFavoriteRequest() {
    }

    public List<FavoriteTargetItem> getTargets() {
        return targets;
    }

    public void setTargets(List<FavoriteTargetItem> targets) {
        this.targets = targets;
    }

    /**
     * 收藏目标项
     */
    public static class FavoriteTargetItem {
        private String targetId;
        private FavoriteTargetType targetType;

        public FavoriteTargetItem() {
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
}
