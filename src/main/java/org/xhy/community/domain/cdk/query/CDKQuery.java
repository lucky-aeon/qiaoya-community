package org.xhy.community.domain.cdk.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;

public class CDKQuery extends BasePageQuery {
    
    private CDKType cdkType;
    private String targetId;
    private CDKStatus status;
    private CDKAcquisitionType acquisitionType; // 新增
    private String code;
    
    public CDKQuery() {
    }
    
    public CDKQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public CDKType getCdkType() {
        return cdkType;
    }
    
    public void setCdkType(CDKType cdkType) {
        this.cdkType = cdkType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public CDKStatus getStatus() {
        return status;
    }
    
    public void setStatus(CDKStatus status) {
        this.status = status;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CDKAcquisitionType getAcquisitionType() {
        return acquisitionType;
    }

    public void setAcquisitionType(CDKAcquisitionType acquisitionType) {
        this.acquisitionType = acquisitionType;
    }
}