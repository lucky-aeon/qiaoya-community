package org.xhy.community.interfaces.cdk.request;

import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * CDK查询请求
 */
public class CDKQueryRequest extends PageRequest {
    
    private CDKType cdkType;
    private String targetId;
    private CDKStatus status;
    
    public CDKQueryRequest() {
    }
    
    public CDKType getCdkType() { return cdkType; }
    public void setCdkType(CDKType cdkType) { this.cdkType = cdkType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public CDKStatus getStatus() { return status; }
    public void setStatus(CDKStatus status) { this.status = status; }
}