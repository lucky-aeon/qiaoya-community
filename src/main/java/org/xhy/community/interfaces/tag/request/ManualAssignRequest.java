package org.xhy.community.interfaces.tag.request;

import jakarta.validation.constraints.NotBlank;
import org.xhy.community.domain.tag.valueobject.TagSourceType;

public class ManualAssignRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String tagId;
    private TagSourceType sourceType;
    private String sourceId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public TagSourceType getSourceType() { return sourceType; }
    public void setSourceType(TagSourceType sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
}
