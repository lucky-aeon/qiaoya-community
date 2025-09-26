package org.xhy.community.application.cdk.dto;

import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;

import java.time.LocalDateTime;

public class CDKDTO {
    
    private String id;
    private String code;
    private CDKType cdkType;
    private String targetId;
    private String targetName;
    private String batchId;
    private CDKStatus status;
    private String usedByUserId;
    private LocalDateTime usedTime;
    private CDKAcquisitionType acquisitionType; // 新增
    private String remark;                      // 新增
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public CDKDTO() {
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public CDKType getCdkType() { return cdkType; }
    public void setCdkType(CDKType cdkType) { this.cdkType = cdkType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public CDKStatus getStatus() { return status; }
    public void setStatus(CDKStatus status) { this.status = status; }
    
    public String getUsedByUserId() { return usedByUserId; }
    public void setUsedByUserId(String usedByUserId) { this.usedByUserId = usedByUserId; }
    
    public LocalDateTime getUsedTime() { return usedTime; }
    public void setUsedTime(LocalDateTime usedTime) { this.usedTime = usedTime; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public CDKAcquisitionType getAcquisitionType() { return acquisitionType; }
    public void setAcquisitionType(CDKAcquisitionType acquisitionType) { this.acquisitionType = acquisitionType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
