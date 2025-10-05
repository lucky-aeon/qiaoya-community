package org.xhy.community.interfaces.tag.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 添加标签范围请求
 * 当前阶段仅支持绑定课程，因此只需传入 targetId=courseId
 */
public class AddScopeRequest {
    @NotBlank
    private String targetId; // 课程ID

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
