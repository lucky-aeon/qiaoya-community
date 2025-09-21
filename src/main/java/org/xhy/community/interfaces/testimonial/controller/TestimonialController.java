package org.xhy.community.interfaces.testimonial.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.testimonial.dto.TestimonialDTO;
import org.xhy.community.application.testimonial.service.TestimonialAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.testimonial.request.CreateTestimonialRequest;
import org.xhy.community.interfaces.testimonial.request.UpdateTestimonialRequest;

/**
 * 用户学员评价管理控制器
 * 提供用户的评价提交、查看和修改功能
 * @module 学员评价管理
 */
@RestController
@RequestMapping("/api/testimonials")
public class TestimonialController {

    private final TestimonialAppService testimonialAppService;

    public TestimonialController(TestimonialAppService testimonialAppService) {
        this.testimonialAppService = testimonialAppService;
    }

    /**
     * 提交学员评价
     * 用户提交自己的学习成果评价，每个用户只能提交一条评价
     * 需要JWT令牌认证
     *
     * @param request 创建评价请求参数，包含评价内容和评分
     * @return 创建成功的评价详情信息
     */
    @PostMapping
    public ApiResponse<TestimonialDTO> createTestimonial(@Valid @RequestBody CreateTestimonialRequest request) {
        String currentUserId = UserContext.getCurrentUserId();

        TestimonialDTO testimonial = testimonialAppService.createTestimonial(request, currentUserId);
        return ApiResponse.success("提交评价成功", testimonial);
    }

    /**
     * 查看我的评价
     * 用户查看自己提交的评价详情
     * 需要JWT令牌认证
     *
     * @return 用户的评价详情，如果未提交则返回null
     */
    @GetMapping("/my")
    public ApiResponse<TestimonialDTO> getMyTestimonial() {
        String currentUserId = UserContext.getCurrentUserId();

        TestimonialDTO testimonial = testimonialAppService.getMyTestimonial(currentUserId);
        return ApiResponse.success(testimonial);
    }

    /**
     * 更新我的评价
     * 用户修改自己的评价内容，只有待审核状态的评价可以修改
     * 需要JWT令牌认证
     *
     * @param testimonialId 评价ID，UUID格式
     * @param request 更新评价请求参数，包含新的评价内容和评分
     * @return 更新后的评价详情信息
     */
    @PutMapping("/{testimonialId}")
    public ApiResponse<TestimonialDTO> updateMyTestimonial(@PathVariable String testimonialId,
                                                          @Valid @RequestBody UpdateTestimonialRequest request) {
        String currentUserId = UserContext.getCurrentUserId();

        TestimonialDTO testimonial = testimonialAppService.updateMyTestimonial(testimonialId, request, currentUserId);
        return ApiResponse.success("更新评价成功", testimonial);
    }
}