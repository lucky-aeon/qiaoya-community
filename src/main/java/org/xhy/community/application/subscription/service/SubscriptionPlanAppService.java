package org.xhy.community.application.subscription.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.subscription.dto.AppSubscriptionPlanDTO;
import org.xhy.community.application.subscription.assembler.SubscriptionPlanAssembler;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;

import java.util.List;

@Service
public class SubscriptionPlanAppService {

    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public SubscriptionPlanAppService(SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }

    public List<AppSubscriptionPlanDTO> getActiveSubscriptionPlans() {
        List<SubscriptionPlanEntity> entities = subscriptionPlanDomainService.getActiveSubscriptionPlans();
        return SubscriptionPlanAssembler.toAppDTOList(entities);
    }

    public List<AppSubscriptionPlanDTO> getActivePaidSubscriptionPlans() {
        List<SubscriptionPlanEntity> entities = subscriptionPlanDomainService.getActivePaidSubscriptionPlans();
        return SubscriptionPlanAssembler.toAppDTOList(entities);
    }
}