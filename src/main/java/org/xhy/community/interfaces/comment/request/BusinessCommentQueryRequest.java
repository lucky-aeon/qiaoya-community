package org.xhy.community.interfaces.comment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.interfaces.common.request.PageRequest;

public class BusinessCommentQueryRequest extends PageRequest {
    
    @NotBlank(message = "业务ID不能为空")
    private String businessId;
    
    @NotNull(message = "业务类型不能为空")
    private BusinessType businessType;
    
    public BusinessCommentQueryRequest() {
        super();
    }
    
    public BusinessCommentQueryRequest(String businessId, BusinessType businessType, Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
        this.businessId = businessId;
        this.businessType = businessType;
    }
    
    // Getters and Setters
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    
    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
}