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
        cdkDomainService.activateCDK(userId, request.getCdkCode());
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
     * 兜底：确保用户至少拥有默认套餐
     * - 若用户无有效订阅，则尝试绑定默认套餐
     * - 若默认套餐未配置/无效或绑定失败，返回false且不抛异常
     * @return 是否执行了绑定（成功绑定返回true，已存在或未配置返回false）
     */
    public boolean ensureDefaultSubscriptionIfMissing(String userId) {
        try {
            var actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
            if (actives != null && !actives.isEmpty()) {
                return false;
            }

            DefaultSubscriptionConfig config = systemConfigDomainService.getDefaultSubscriptionConfig();
            if (config == null || !config.isValid()) {
                return false;
            }

            UserSubscriptionEntity created = subscriptionDomainService.createSystemGiftSubscription(
                userId, config.getSubscriptionPlanId()
            );
            return created != null;
        } catch (Exception e) {
            log.warn("ensureDefaultSubscriptionIfMissing failed, userId={}, err={}", userId, e.getMessage());
            return false;
        }
    }
}
