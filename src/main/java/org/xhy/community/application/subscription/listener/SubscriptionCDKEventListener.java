package org.xhy.community.application.subscription.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;

@Component
public class SubscriptionCDKEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCDKEventListener.class);

    private final SubscriptionDomainService subscriptionDomainService;

    public SubscriptionCDKEventListener(SubscriptionDomainService subscriptionDomainService) {
        this.subscriptionDomainService = subscriptionDomainService;
    }

    @EventListener
    public void handleCDKActivated(CDKActivatedEvent event) {
        if (event.getCdkType() == CDKType.SUBSCRIPTION_PLAN) {
            String cdkCode = event.getCdkCode();
            log.info("[CDK激活-监听] 处理套餐CDK: userId={}, planId={}, cdk={}",
                    event.getUserId(), event.getTargetId(), cdkCode);

            UserSubscriptionEntity created = subscriptionDomainService.createSubscriptionFromCDK(
                event.getUserId(),
                event.getTargetId(),
                    cdkCode
            );

            if (created != null) {
                log.info("[CDK激活-监听] 已创建订阅: subscriptionId={}, userId={}, planId={}, endTime={}",
                        created.getId(), created.getUserId(), created.getSubscriptionPlanId(), created.getEndTime());
            }
        }
    }

    private String mask(String code) {
        if (code == null || code.length() <= 4) return "****";
        int len = code.length();
        return code.substring(0, Math.min(4, len)) + "****" + code.substring(len - 2);
    }
}
