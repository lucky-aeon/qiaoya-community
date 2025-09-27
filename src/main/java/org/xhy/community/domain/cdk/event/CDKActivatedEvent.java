package org.xhy.community.domain.cdk.event;

import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;

import java.time.LocalDateTime;

/**
 * CDK激活事件 - 扩展版本，包含订单所需信息
 */
public class CDKActivatedEvent {
    
    private final String userId;          // 用户ID
    private final String cdkCode;         // CDK码
    private final CDKType cdkType;        // CDK类型
    private final String targetId;        // 目标ID（课程ID等）
    private final CDKAcquisitionType acquisitionType; // CDK获得方式
    private final LocalDateTime activatedTime; // 激活时间


    public CDKActivatedEvent(String userId, String cdkCode, CDKType cdkType, String targetId,
                           CDKAcquisitionType acquisitionType
                           ) {
        this.userId = userId;
        this.cdkCode = cdkCode;
        this.cdkType = cdkType;
        this.targetId = targetId;
        this.acquisitionType = acquisitionType;
        this.activatedTime = LocalDateTime.now();
    }
    
    /**
     * 简化构造函数 - 用于兼容现有代码
     * TODO: 后续优化为从Application层传入完整信息
     */
    public CDKActivatedEvent(String userId, String cdkCode, CDKType cdkType, String targetId) {
        this.userId = userId;
        this.cdkCode = cdkCode;
        this.cdkType = cdkType;
        this.targetId = targetId;
        this.acquisitionType = CDKAcquisitionType.PURCHASE; // 默认为购买
        this.activatedTime = LocalDateTime.now();
    }
    
    // 基础信息Getters
    public String getUserId() { return userId; }
    public String getCdkCode() { return cdkCode; }
    public CDKType getCdkType() { return cdkType; }
    public String getTargetId() { return targetId; }
    public CDKAcquisitionType getAcquisitionType() { return acquisitionType; }
    public LocalDateTime getActivatedTime() { return activatedTime; }
}
