package org.xhy.community.interfaces.reaction.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ToggleReactionRequest {

    @NotBlank
    private String businessType; // 使用枚举字符串：POST/COMMENT/COURSE/CHAPTER/MEETING/AI_NEWS

    @NotBlank
    private String businessId;

    @NotBlank
    private String reactionType; // code

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
}

