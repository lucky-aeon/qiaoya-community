package org.xhy.community.interfaces.follow.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

/**
 * 创建关注请求
 */
public class CreateFollowRequest {
    
    /** 被关注目标的ID */
    @NotBlank(message = "目标ID不能为空")
    private String targetId;
    
    /** 关注目标类型 */
    @NotNull(message = "目标类型不能为空")
    private FollowTargetType targetType;
    
    public CreateFollowRequest() {}
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public FollowTargetType getTargetType() { return targetType; }
    public void setTargetType(FollowTargetType targetType) { this.targetType = targetType; }
}