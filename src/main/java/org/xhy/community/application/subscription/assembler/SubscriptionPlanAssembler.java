package org.xhy.community.application.subscription.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.subscription.dto.SubscriptionPlanDTO;
import org.xhy.community.application.subscription.dto.SimpleSubscriptionPlanDTO;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.query.SubscriptionPlanQuery;
import org.xhy.community.interfaces.subscription.request.CreateSubscriptionPlanRequest;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanRequest;
import org.xhy.community.interfaces.subscription.request.SubscriptionPlanQueryRequest;

public class SubscriptionPlanAssembler {
    
    public static SubscriptionPlanQuery fromRequest(SubscriptionPlanQueryRequest request) {
        SubscriptionPlanQuery query = new SubscriptionPlanQuery(request.getPageNum(), request.getPageSize());
        query.setName(request.getName());
        query.setLevel(request.getLevel());
        return query;
    }
    
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
    
    public static SimpleSubscriptionPlanDTO toSimpleDTO(SubscriptionPlanEntity entity) {
        if (entity == null) {
            return null;
        }
        
        SimpleSubscriptionPlanDTO dto = new SimpleSubscriptionPlanDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}