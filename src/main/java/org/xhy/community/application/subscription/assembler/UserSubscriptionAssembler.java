package org.xhy.community.application.subscription.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.application.subscription.dto.CDKActivationResultDTO;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService.CDKActivationResult;

public class UserSubscriptionAssembler {
    
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
    
    public static CDKActivationResultDTO toActivationResultDTO(CDKActivationResult result) {
        if (result == null) {
            return null;
        }
        
        UserSubscriptionDTO subscriptionDTO = null;
        if (result.getSubscription() != null) {
            subscriptionDTO = toDTO(result.getSubscription());
        }
        
        return new CDKActivationResultDTO(
            result.isSuccess(),
            result.getMessage(),
            result.getTargetName(),
            subscriptionDTO
        );
    }
}