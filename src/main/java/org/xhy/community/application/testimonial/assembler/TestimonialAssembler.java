package org.xhy.community.application.testimonial.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.testimonial.dto.AdminTestimonialDTO;
import org.xhy.community.application.testimonial.dto.TestimonialDTO;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.interfaces.testimonial.request.CreateTestimonialRequest;
import org.xhy.community.interfaces.testimonial.request.UpdateTestimonialRequest;

import org.xhy.community.domain.testimonial.query.TestimonialQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.interfaces.testimonial.request.QueryTestimonialRequest;

import java.util.List;
import java.util.Map;

public class TestimonialAssembler {

    public static TestimonialDTO toDTO(TestimonialEntity entity) {
        if (entity == null) {
            return null;
        }

        TestimonialDTO dto = new TestimonialDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static List<TestimonialDTO> toDTOList(List<TestimonialEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(TestimonialAssembler::toDTO)
                .toList();
    }

    public static AdminTestimonialDTO toAdminDTO(TestimonialEntity entity, Map<String, UserEntity> userMap) {
        if (entity == null) {
            return null;
        }

        AdminTestimonialDTO dto = new AdminTestimonialDTO();
        BeanUtils.copyProperties(entity, dto);

        if (userMap != null) {
            UserEntity user = userMap.get(entity.getUserId());
            if (user != null) {
                dto.setUserName(user.getName());
            }
        }

        return dto;
    }

    public static List<AdminTestimonialDTO> toAdminDTOList(List<TestimonialEntity> entities, Map<String, UserEntity> userMap) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(entity -> toAdminDTO(entity, userMap))
                .toList();
    }

    public static TestimonialQuery fromAdminRequest(QueryTestimonialRequest request) {
        TestimonialQuery query = new TestimonialQuery(request.getPageNum(), request.getPageSize());
        query.setStatus(request.getStatus());
        query.setAccessLevel(AccessLevel.ADMIN);
        query.setCurrentUserId(null);
        return query;
    }

    public static TestimonialQuery fromUserRequest(QueryTestimonialRequest request, String userId) {
        TestimonialQuery query = new TestimonialQuery(request.getPageNum(), request.getPageSize());
        query.setStatus(request.getStatus());
        query.setAccessLevel(AccessLevel.USER);
        query.setCurrentUserId(userId);
        return query;
    }

    public static TestimonialEntity fromCreateRequest(CreateTestimonialRequest request, String userId) {
        if (request == null) {
            return null;
        }

        return new TestimonialEntity(userId, request.getContent(), request.getRating());
    }

    public static TestimonialEntity fromUpdateRequest(UpdateTestimonialRequest request, String testimonialId) {
        if (request == null) {
            return null;
        }

        TestimonialEntity entity = new TestimonialEntity();
        entity.setId(testimonialId);
        entity.setContent(request.getContent());
        entity.setRating(request.getRating());
        return entity;
    }
}