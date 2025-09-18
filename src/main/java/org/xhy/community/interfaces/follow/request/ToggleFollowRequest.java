package org.xhy.community.interfaces.follow.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

/**
 * 关注切换请求
 * 用于前台页面的关注/取消关注切换操作
 */
public class ToggleFollowRequest {
    
    /** 被关注目标的ID */
    @NotBlank(message = "目标ID不能为空")
    private String targetId;
    
    /** 关注目标类型 */
    @NotNull(message = "目标类型不能为空")
    private FollowTargetType targetType;
    
    public ToggleFollowRequest() {}
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public FollowTargetType getTargetType() { return targetType; }
    public void setTargetType(FollowTargetType targetType) { this.targetType = targetType; }
}