package org.xhy.community.interfaces.reaction.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ReactionSummaryBatchRequest {

    @NotBlank
    private String businessType;

    @NotEmpty
    private List<String> businessIds;

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public List<String> getBusinessIds() { return businessIds; }
    public void setBusinessIds(List<String> businessIds) { this.businessIds = businessIds; }
}

