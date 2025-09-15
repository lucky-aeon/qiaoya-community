package org.xhy.community.application.subscription.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;

@Component
public class SubscriptionCDKEventListener {
    
    private final SubscriptionDomainService subscriptionDomainService;
    
    public SubscriptionCDKEventListener(SubscriptionDomainService subscriptionDomainService) {
        this.subscriptionDomainService = subscriptionDomainService;
    }
    
    @EventListener
    public void handleCDKActivated(CDKActivatedEvent event) {
        if (event.getCdkType() == CDKType.SUBSCRIPTION_PLAN) {
            // 处理套餐CDK激活逻辑
            subscriptionDomainService.createSubscriptionFromCDK(
                event.getUserId(), 
                event.getTargetId(), 
                event.getCdkCode()
            );
        }
    }
}