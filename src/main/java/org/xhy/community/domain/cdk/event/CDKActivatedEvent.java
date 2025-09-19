package org.xhy.community.domain.cdk.event;

import org.xhy.community.domain.cdk.valueobject.CDKType;

import java.time.LocalDateTime;

/**
 * CDK激活事件 - 包含完整的通知所需信息
 */
public class CDKActivatedEvent {
    
    private final String userId;          // 用户ID
    private final String cdkCode;         // CDK码
    private final CDKType cdkType;        // CDK类型
    private final String targetId;        // 目标ID（课程ID等）
    private final LocalDateTime activatedTime; // 激活时间
    
    // 包含完整的业务信息，避免通知领域跨领域查询
    private final String userName;        // 用户姓名
    private final String userEmail;       // 用户邮箱
    
    public CDKActivatedEvent(String userId, String cdkCode, CDKType cdkType, String targetId,
                           String userName, String userEmail) {
        this.userId = userId;
        this.cdkCode = cdkCode;
        this.cdkType = cdkType;
        this.targetId = targetId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.activatedTime = LocalDateTime.now();
    }
    
    /**
     * 简化构造函数 - 用于兼容现有代码
     * TODO: 后续优化为从Application层传入完整用户信息
     */
    public CDKActivatedEvent(String userId, String cdkCode, CDKType cdkType, String targetId) {
        this.userId = userId;
        this.cdkCode = cdkCode;
        this.cdkType = cdkType;
        this.targetId = targetId;
        this.userName = "待获取";  // 占位符，通知监听器需要补充
        this.userEmail = null;
        this.activatedTime = LocalDateTime.now();
    }
    
    // 基础信息Getters
    public String getUserId() { return userId; }
    public String getCdkCode() { return cdkCode; }
    public CDKType getCdkType() { return cdkType; }
    public String getTargetId() { return targetId; }
    public LocalDateTime getActivatedTime() { return activatedTime; }
    
    // 完整业务信息Getters
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    
    // 为了兼容现有代码，保留旧方法
    public LocalDateTime getActivationTime() { return activatedTime; }
}