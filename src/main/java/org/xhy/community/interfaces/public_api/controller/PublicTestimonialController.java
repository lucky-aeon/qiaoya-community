package org.xhy.community.interfaces.public_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.testimonial.dto.PublicTestimonialDTO;
import org.xhy.community.application.testimonial.service.PublicTestimonialAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

/**
 * 公开学员评价控制器
 * 提供公开访问的学员评价接口，无需用户认证
 * @module 公开API
 */
@RestController
@RequestMapping("/api/public")
public class PublicTestimonialController {

    private final PublicTestimonialAppService publicTestimonialAppService;

    public PublicTestimonialController(PublicTestimonialAppService publicTestimonialAppService) {
        this.publicTestimonialAppService = publicTestimonialAppService;
    }

    /**
     * 获取所有已发布的学员评价
     * 前台展示用，返回所有已发布状态的评价，按排序权重和创建时间倒序排列
     * 无需用户认证
     *
     * @return 已发布的学员评价列表，包含id、用户昵称、内容、评分
     */
    @GetMapping("/testimonials")
    public ApiResponse<List<PublicTestimonialDTO>> getPublishedTestimonials() {
        List<PublicTestimonialDTO> testimonials = publicTestimonialAppService.getPublishedTestimonials();
        return ApiResponse.success(testimonials);

    }
}