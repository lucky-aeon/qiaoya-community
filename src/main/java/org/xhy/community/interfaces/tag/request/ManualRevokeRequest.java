package org.xhy.community.interfaces.tag.request;

import jakarta.validation.constraints.NotBlank;

public class ManualRevokeRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String tagId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
}

