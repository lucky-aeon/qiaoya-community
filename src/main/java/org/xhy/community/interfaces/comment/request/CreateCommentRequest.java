package org.xhy.community.interfaces.comment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.xhy.community.domain.comment.valueobject.BusinessType;

public class CreateCommentRequest {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容长度不能超过2000个字符")
    private String content;
    
    @NotBlank(message = "业务ID不能为空")
    private String businessId;
    
    @NotNull(message = "业务类型不能为空")
    private BusinessType businessType;
    
    public CreateCommentRequest() {
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    
    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
}