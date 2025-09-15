package org.xhy.community.application.subscription.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.subscription.assembler.UserSubscriptionAssembler;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.domain.cdk.service.CDKDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.subscription.request.ActivateCDKRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionQueryRequest;

@Service
public class UserSubscriptionAppService {
    
    private final CDKDomainService cdkDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    
    public UserSubscriptionAppService(CDKDomainService cdkDomainService,
                                    SubscriptionDomainService subscriptionDomainService,
                                    SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.cdkDomainService = cdkDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }
    
    @Transactional
    public void activateCDK(String userId, ActivateCDKRequest request) {
        // 同步处理CDK激活，所有事件监听器都在同一个事务中执行
        cdkDomainService.activateCDK(userId, request.getCdkCode());
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
        return subscriptionPlanDomainService.getSubscriptionPlanById(planId).getName();
    }
}