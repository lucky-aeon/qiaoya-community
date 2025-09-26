package org.xhy.community.application.subscription.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;

import java.util.*;

@Service
public class UserMenuAppService {

    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public UserMenuAppService(SubscriptionDomainService subscriptionDomainService,
                              SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }

    /**
     * 获取用户可见菜单码（有效订阅的并集）
     */
    public List<String> getUserMenuCodes(String userId) {
        List<UserSubscriptionEntity> actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        if (actives == null || actives.isEmpty()) return List.of();

        Set<String> set = new LinkedHashSet<>();
        for (UserSubscriptionEntity sub : actives) {
            List<String> codes = subscriptionPlanDomainService.getSubscriptionPlanMenuCodes(sub.getSubscriptionPlanId());
            if (codes != null) set.addAll(codes);
        }
        return new ArrayList<>(set);
    }
}

