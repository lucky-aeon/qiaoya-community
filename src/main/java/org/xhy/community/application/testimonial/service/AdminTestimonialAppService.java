package org.xhy.community.application.testimonial.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.testimonial.assembler.TestimonialAssembler;
import org.xhy.community.application.testimonial.dto.AdminTestimonialDTO;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.testimonial.service.TestimonialDomainService;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.testimonial.request.ChangeStatusRequest;
import org.xhy.community.interfaces.testimonial.request.QueryTestimonialRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xhy.community.domain.testimonial.query.TestimonialQuery;

@Service
public class AdminTestimonialAppService {

    private final TestimonialDomainService testimonialDomainService;
    private final UserDomainService userDomainService;

    public AdminTestimonialAppService(TestimonialDomainService testimonialDomainService,
                                    UserDomainService userDomainService) {
        this.testimonialDomainService = testimonialDomainService;
        this.userDomainService = userDomainService;
    }

    public IPage<AdminTestimonialDTO> getAllTestimonials(QueryTestimonialRequest request) {
        TestimonialQuery query = TestimonialAssembler.fromAdminRequest(request);
        IPage<TestimonialEntity> testimonialsPage = testimonialDomainService.getTestimonials(query);

        return testimonialsPage.convert(this::convertToAdminDTO);
    }

    public AdminTestimonialDTO changeTestimonialStatus(String testimonialId, ChangeStatusRequest request) {
        TestimonialEntity updatedTestimonial = testimonialDomainService.changeStatus(
            testimonialId, request.getStatus()
        );

        return convertToAdminDTO(updatedTestimonial);
    }

    public void deleteTestimonial(String testimonialId) {
        testimonialDomainService.deleteTestimonial(testimonialId);
    }

    public AdminTestimonialDTO updateSortOrder(String testimonialId, Integer sortOrder) {
        TestimonialEntity updatedTestimonial = testimonialDomainService.updateSortOrder(
            testimonialId, sortOrder
        );

        return convertToAdminDTO(updatedTestimonial);
    }

    private AdminTestimonialDTO convertToAdminDTO(TestimonialEntity entity) {
        if (entity == null) {
            return null;
        }

        // 批量查询用户名，避免N+1查询问题
        Set<String> userIds = Set.of(entity.getUserId());
        Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(userIds);

        return TestimonialAssembler.toAdminDTO(entity, userMap);
    }
}