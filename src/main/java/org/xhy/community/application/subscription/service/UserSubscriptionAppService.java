package org.xhy.community.application.subscription.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.subscription.assembler.UserSubscriptionAssembler;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.domain.cdk.service.CDKDomainService;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.DefaultSubscriptionConfig;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.query.SubscriptionQuery;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.subscription.request.ActivateCDKRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionQueryRequest;

@Service
public class UserSubscriptionAppService {

    private static final Logger log = LoggerFactory.getLogger(UserSubscriptionAppService.class);

    private final CDKDomainService cdkDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final SystemConfigDomainService systemConfigDomainService;

    public UserSubscriptionAppService(CDKDomainService cdkDomainService,
                                      SubscriptionDomainService subscriptionDomainService,
                                      SubscriptionPlanDomainService subscriptionPlanDomainService,
                                      SystemConfigDomainService systemConfigDomainService) {
        this.cdkDomainService = cdkDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.systemConfigDomainService = systemConfigDomainService;
    }
    
    @Transactional
    public void activateCDK(String userId, ActivateCDKRequest request) {
        // 同步处理CDK激活，所有事件监听器都在同一个事务中执行
        String masked = maskCdk(request.getCdkCode());
        log.info("[CDK激活] 发起激活请求: userId={}, cdk={}", userId, masked);
        try {
            cdkDomainService.activateCDK(userId, request.getCdkCode());
            log.info("[CDK激活] 激活成功: userId={}, cdk={}", userId, masked);
        } catch (Exception e) {
            // 保持原有异常传播，同时记录日志（不打印完整CDK）
            log.warn("[CDK激活] 激活失败: userId={}, cdk={}, err={}", userId, masked, e.getMessage());
            throw e;
        }
    }
    
    public IPage<UserSubscriptionDTO> getSubscriptions(String userId, SubscriptionQueryRequest request) {
        SubscriptionQuery query = UserSubscriptionAssembler.fromRequest(userId, request);
        IPage<UserSubscriptionEntity> entityPage = subscriptionDomainService.getPagedUserSubscriptions(query);
        
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

    /**
     * 只读校验：用户是否存在有效订阅
     */
    public boolean hasActiveSubscription(String userId) {
        var actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        return actives != null && !actives.isEmpty();
    }

    private String maskCdk(String code) {
        if (code == null || code.length() <= 4) return "****";
        int len = code.length();
        return code.substring(0, Math.min(4, len)) + "****" + code.substring(len - 2);
    }
}
