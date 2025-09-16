package org.xhy.community.application.subscription.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.query.SubscriptionQuery;
import org.xhy.community.interfaces.subscription.request.SubscriptionQueryRequest;

public class UserSubscriptionAssembler {
    
    public static SubscriptionQuery fromRequest(String userId, SubscriptionQueryRequest request) {
        SubscriptionQuery query = new SubscriptionQuery(request.getPageNum(), request.getPageSize());
        query.setUserId(userId);
        return query;
    }
    
    public static UserSubscriptionDTO toDTO(UserSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setDaysRemaining(entity.getDaysRemaining());
        dto.setActive(entity.isActive());
        return dto;
    }
    
    public static UserSubscriptionDTO toDTOWithPlanName(UserSubscriptionEntity entity, String planName) {
        UserSubscriptionDTO dto = toDTO(entity);
        if (dto != null) {
            dto.setSubscriptionPlanName(planName);
        }
        return dto;
    }
}