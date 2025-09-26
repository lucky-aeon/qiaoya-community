package org.xhy.community.application.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanPermissionsRequest;
import org.xhy.community.application.subscription.dto.PermissionOptionDTO;
import org.xhy.community.infrastructure.permission.PermissionCodeScanner;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminSubscriptionPlanPermissionAppService {

    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final PermissionCodeScanner permissionCodeScanner;

    public AdminSubscriptionPlanPermissionAppService(SubscriptionPlanDomainService subscriptionPlanDomainService,
                                                     PermissionCodeScanner permissionCodeScanner) {
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.permissionCodeScanner = permissionCodeScanner;
    }

    public List<String> getSubscriptionPlanPermissionCodes(String planId) {
        return subscriptionPlanDomainService.getSubscriptionPlanPermissionCodes(planId);
    }

    @Transactional
    public void updateSubscriptionPlanPermissions(String planId, UpdateSubscriptionPlanPermissionsRequest request) {
        List<String> permissions = request == null ? List.of() : (request.getPermissions() == null ? List.of() : request.getPermissions());
        List<String> normalized = permissions.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
        subscriptionPlanDomainService.syncSubscriptionPlanPermissions(planId, normalized);
    }

    public List<PermissionOptionDTO> getPermissionOptions() {
        // 基于项目内注解动态扫描的结果生成选项（name 来自注解 items；未提供名称时用 code 兜底）
        return permissionCodeScanner.getDiscoveredOptions();
    }

    /** 动态扫描到的权限码（来自项目中注解） */
    public List<String> getDiscoveredPermissionCodes() {
        return permissionCodeScanner.getDiscoveredCodes();
    }
}
