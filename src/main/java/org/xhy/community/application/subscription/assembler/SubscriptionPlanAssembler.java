package org.xhy.community.application.subscription.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.subscription.dto.SubscriptionPlanDTO;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.interfaces.subscription.request.CreateSubscriptionPlanRequest;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanRequest;

public class SubscriptionPlanAssembler {
    
    public static SubscriptionPlanDTO toDTO(SubscriptionPlanEntity entity) {
        if (entity == null) {
            return null;
        }
        
        SubscriptionPlanDTO dto = new SubscriptionPlanDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static SubscriptionPlanEntity fromCreateRequest(CreateSubscriptionPlanRequest request) {
        if (request == null) {
            return null;
        }
        
        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
        BeanUtils.copyProperties(request, entity);
        return entity;
    }
    
    public static SubscriptionPlanEntity fromUpdateRequest(UpdateSubscriptionPlanRequest request, String id) {
        if (request == null) {
            return null;
        }
        
        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        return entity;
    }
}