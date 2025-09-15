package org.xhy.community.domain.cdk.event;

import org.xhy.community.domain.cdk.valueobject.CDKType;

import java.time.LocalDateTime;

public class CDKActivatedEvent {
    
    private final String userId;
    private final String cdkCode;
    private final CDKType cdkType;
    private final String targetId;
    private final LocalDateTime activatedTime;
    
    public CDKActivatedEvent(String userId, String cdkCode, CDKType cdkType, String targetId) {
        this.userId = userId;
        this.cdkCode = cdkCode;
        this.cdkType = cdkType;
        this.targetId = targetId;
        this.activatedTime = LocalDateTime.now();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getCdkCode() {
        return cdkCode;
    }
    
    public CDKType getCdkType() {
        return cdkType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public LocalDateTime getActivatedTime() {
        return activatedTime;
    }
}