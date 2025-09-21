package org.xhy.community.application.testimonial.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.testimonial.assembler.TestimonialAssembler;
import org.xhy.community.application.testimonial.dto.TestimonialDTO;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.testimonial.service.TestimonialDomainService;

import java.util.List;

@Service
public class PublicTestimonialAppService {

    private final TestimonialDomainService testimonialDomainService;

    public PublicTestimonialAppService(TestimonialDomainService testimonialDomainService) {
        this.testimonialDomainService = testimonialDomainService;
    }

    public List<TestimonialDTO> getPublishedTestimonials() {
        List<TestimonialEntity> testimonials = testimonialDomainService.getPublishedTestimonials();
        return TestimonialAssembler.toDTOList(testimonials);
    }
}