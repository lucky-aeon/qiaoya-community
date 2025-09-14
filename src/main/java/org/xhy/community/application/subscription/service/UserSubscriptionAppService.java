package org.xhy.community.application.subscription.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.subscription.assembler.UserSubscriptionAssembler;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.application.subscription.dto.CDKActivationResultDTO;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService.CDKActivationResult;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.subscription.request.ActivateCDKRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionQueryRequest;

@Service
public class UserSubscriptionAppService {
    
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    
    public UserSubscriptionAppService(SubscriptionDomainService subscriptionDomainService,
                                    SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }
    
    public CDKActivationResultDTO activateCDK(String userId, ActivateCDKRequest request) {
        CDKActivationResult result = subscriptionDomainService.activateCDK(userId, request.getCdkCode());
        return UserSubscriptionAssembler.toActivationResultDTO(result);
    }
    
    public IPage<UserSubscriptionDTO> getSubscriptions(String userId, SubscriptionQueryRequest request) {
        IPage<UserSubscriptionEntity> entityPage = subscriptionDomainService.getPagedUserSubscriptions(
            userId, 
            request.getPageNum(), 
            request.getPageSize()
        );
        
        return entityPage.convert(entity -> {
            String planName = getPlanName(entity.getSubscriptionPlanId());
            return UserSubscriptionAssembler.toDTOWithPlanName(entity, planName);
        });
    }
    
    public UserSubscriptionDTO getSubscriptionDetail(String userId, String subscriptionId) {
        UserSubscriptionEntity subscription = subscriptionDomainService.getSubscriptionById(subscriptionId,userId);

        String planName = getPlanName(subscription.getSubscriptionPlanId());
        return UserSubscriptionAssembler.toDTOWithPlanName(subscription, planName);
    }
    
    private String getPlanName(String planId) {
        try {
            return subscriptionPlanDomainService.getSubscriptionPlanById(planId).getName();
        } catch (Exception e) {
            return "未知套餐";
        }
    }
}