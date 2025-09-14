package org.xhy.community.application.subscription.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.subscription.assembler.SubscriptionPlanAssembler;
import org.xhy.community.application.subscription.dto.SubscriptionPlanDTO;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanCourseEntity;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.subscription.request.CreateSubscriptionPlanRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionPlanQueryRequest;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanRequest;

import java.util.List;

@Service
public class AdminSubscriptionPlanAppService {
    
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    
    public AdminSubscriptionPlanAppService(SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }
    
    public SubscriptionPlanDTO createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        SubscriptionPlanEntity entity = SubscriptionPlanAssembler.fromCreateRequest(request);
        SubscriptionPlanEntity created = subscriptionPlanDomainService.createSubscriptionPlan(entity);
        
        return SubscriptionPlanAssembler.toDTO(created);
    }
    
    public SubscriptionPlanDTO updateSubscriptionPlan(String id, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlanEntity entity = SubscriptionPlanAssembler.fromUpdateRequest(request, id);
        SubscriptionPlanEntity updated = subscriptionPlanDomainService.updateSubscriptionPlan(entity);
        
        return SubscriptionPlanAssembler.toDTO(updated);
    }
    
    public SubscriptionPlanDTO getSubscriptionPlanById(String id) {
        SubscriptionPlanEntity entity = subscriptionPlanDomainService.getSubscriptionPlanById(id);
        return SubscriptionPlanAssembler.toDTO(entity);
    }
    
    public void deleteSubscriptionPlan(String id) {
        subscriptionPlanDomainService.deleteSubscriptionPlan(id);
    }
    
    public IPage<SubscriptionPlanDTO> getPagedSubscriptionPlans(SubscriptionPlanQueryRequest request) {
        IPage<SubscriptionPlanEntity> entityPage = subscriptionPlanDomainService.getPagedSubscriptionPlans(
            request.getPageNum(), request.getPageSize(), request.getName(), request.getLevel()
        );
        
        return entityPage.convert(SubscriptionPlanAssembler::toDTO);
    }
}