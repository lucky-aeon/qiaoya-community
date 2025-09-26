package org.xhy.community.domain.cdk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;

import java.time.LocalDateTime;

@TableName("cdk_codes")
public class CDKEntity extends BaseEntity {
    
    private String code;
    private CDKType cdkType;
    private String targetId;
    private String batchId;
    private CDKStatus status;
    private String usedByUserId;
    private LocalDateTime usedTime;

    // 新增字段
    private CDKAcquisitionType acquisitionType;  // CDK获得方式
    private String remark;                       // 备注信息
    
    public CDKEntity() {
    }
    
    public CDKEntity(String code, CDKType cdkType, String targetId, String batchId) {
        this.code = code;
        this.cdkType = cdkType;
        this.targetId = targetId;
        this.batchId = batchId;
        this.status = CDKStatus.ACTIVE;
        this.acquisitionType = CDKAcquisitionType.PURCHASE; // 默认为购买
    }

    public CDKEntity(String code, CDKType cdkType, String targetId, String batchId,
                     CDKAcquisitionType acquisitionType) {
        this(code, cdkType, targetId, batchId);
        this.acquisitionType = acquisitionType;
    }
    
    public void markAsUsed(String userId) {
        this.status = CDKStatus.USED;
        this.usedByUserId = userId;
        this.usedTime = LocalDateTime.now();
    }
    
    public void disable() {
        this.status = CDKStatus.DISABLED;
    }
    
    public boolean isUsable() {
        return this.status == CDKStatus.ACTIVE;
    }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public CDKType getCdkType() { return cdkType; }
    public void setCdkType(CDKType cdkType) { this.cdkType = cdkType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public CDKStatus getStatus() { return status; }
    public void setStatus(CDKStatus status) { this.status = status; }
    
    public String getUsedByUserId() { return usedByUserId; }
    public void setUsedByUserId(String usedByUserId) { this.usedByUserId = usedByUserId; }
    
    public LocalDateTime getUsedTime() { return usedTime; }
    public void setUsedTime(LocalDateTime usedTime) { this.usedTime = usedTime; }

    public CDKAcquisitionType getAcquisitionType() { return acquisitionType; }
    public void setAcquisitionType(CDKAcquisitionType acquisitionType) { this.acquisitionType = acquisitionType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
