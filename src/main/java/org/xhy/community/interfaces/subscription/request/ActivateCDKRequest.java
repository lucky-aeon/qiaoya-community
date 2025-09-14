package org.xhy.community.interfaces.subscription.request;

import jakarta.validation.constraints.NotBlank;

/**
 * CDK激活请求
 */
public class ActivateCDKRequest {
    
    @NotBlank(message = "CDK码不能为空")
    private String cdkCode;
    
    public ActivateCDKRequest() {
    }
    
    public String getCdkCode() { return cdkCode; }
    public void setCdkCode(String cdkCode) { this.cdkCode = cdkCode; }
}