package org.xhy.community.application.testimonial.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.testimonial.dto.PublicTestimonialDTO;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Map;

public class PublicTestimonialAssembler {

    public static PublicTestimonialDTO toPublicDTO(TestimonialEntity entity, Map<String, UserEntity> userMap) {
        if (entity == null) {
            return null;
        }

        PublicTestimonialDTO dto = new PublicTestimonialDTO();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setRating(entity.getRating());

        if (userMap != null) {
            UserEntity user = userMap.get(entity.getUserId());
            if (user != null) {
                dto.setUserNickname(user.getName());
            }
        }

        return dto;
    }

    public static List<PublicTestimonialDTO> toPublicDTOList(List<TestimonialEntity> entities, Map<String, UserEntity> userMap) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(entity -> toPublicDTO(entity, userMap))
                .toList();
    }
}