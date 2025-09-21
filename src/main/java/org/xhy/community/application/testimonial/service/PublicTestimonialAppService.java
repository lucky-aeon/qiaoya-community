package org.xhy.community.application.testimonial.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.testimonial.assembler.PublicTestimonialAssembler;
import org.xhy.community.application.testimonial.dto.PublicTestimonialDTO;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.testimonial.service.TestimonialDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PublicTestimonialAppService {

    private final TestimonialDomainService testimonialDomainService;
    private final UserDomainService userDomainService;

    public PublicTestimonialAppService(TestimonialDomainService testimonialDomainService,
                                     UserDomainService userDomainService) {
        this.testimonialDomainService = testimonialDomainService;
        this.userDomainService = userDomainService;
    }

    public List<PublicTestimonialDTO> getPublishedTestimonials() {
        List<TestimonialEntity> testimonials = testimonialDomainService.getPublishedTestimonials();

        // 批量查询关联的用户信息，避免N+1查询问题
        Set<String> userIds = testimonials.stream()
                .map(TestimonialEntity::getUserId)
                .collect(Collectors.toSet());

        Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(userIds);

        return PublicTestimonialAssembler.toPublicDTOList(testimonials, userMap);
    }
}