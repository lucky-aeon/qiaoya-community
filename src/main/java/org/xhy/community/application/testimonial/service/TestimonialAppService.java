package org.xhy.community.application.testimonial.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.testimonial.assembler.TestimonialAssembler;
import org.xhy.community.application.testimonial.dto.TestimonialDTO;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.testimonial.service.TestimonialDomainService;
import org.xhy.community.interfaces.testimonial.request.CreateTestimonialRequest;
import org.xhy.community.interfaces.testimonial.request.UpdateTestimonialRequest;

@Service
public class TestimonialAppService {

    private final TestimonialDomainService testimonialDomainService;

    public TestimonialAppService(TestimonialDomainService testimonialDomainService) {
        this.testimonialDomainService = testimonialDomainService;
    }

    public TestimonialDTO createTestimonial(CreateTestimonialRequest request, String userId) {
        TestimonialEntity testimonial = testimonialDomainService.createTestimonial(
            userId, request.getContent(), request.getRating()
        );

        return TestimonialAssembler.toDTO(testimonial);
    }

    public TestimonialDTO getMyTestimonial(String userId) {
        TestimonialEntity testimonial = testimonialDomainService.getUserTestimonial(userId);
        return TestimonialAssembler.toDTO(testimonial);
    }

    public TestimonialDTO updateMyTestimonial(String testimonialId, UpdateTestimonialRequest request, String userId) {
        // 统一更新路径：Request -> Assembler -> Entity -> Domain.update(entity)
        TestimonialEntity patch = TestimonialAssembler.fromUpdateRequest(request, testimonialId);
        TestimonialEntity updated = testimonialDomainService.updateTestimonialIfPending(patch, userId);
        return TestimonialAssembler.toDTO(updated);
    }
}
