package org.xhy.community.interfaces.testimonial.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.testimonial.dto.AdminTestimonialDTO;
import org.xhy.community.application.testimonial.service.AdminTestimonialAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.testimonial.request.ChangeStatusRequest;
import org.xhy.community.interfaces.testimonial.request.QueryTestimonialRequest;

/**
 * 管理员学员评价管理控制器
 * 提供管理员对所有学员评价的审核和管理功能
 * @module 学员评价管理
 */
@RestController
@RequestMapping("/api/admin/testimonials")
public class AdminTestimonialController {

    private final AdminTestimonialAppService adminTestimonialAppService;

    public AdminTestimonialController(AdminTestimonialAppService adminTestimonialAppService) {
        this.adminTestimonialAppService = adminTestimonialAppService;
    }

    /**
     * 分页查询所有学员评价
     * 管理员查看所有用户的评价，包含用户名和审核状态
     * 需要管理员权限认证
     *
     * @param request 查询请求参数，包含分页信息和状态筛选
     * @return 包含完整信息的评价分页列表
     */
    @GetMapping
    public ApiResponse<IPage<AdminTestimonialDTO>> getAllTestimonials(QueryTestimonialRequest request) {
        IPage<AdminTestimonialDTO> testimonials = adminTestimonialAppService.getAllTestimonials(request);
        return ApiResponse.success(testimonials);
    }

    /**
     * 修改评价状态
     * 管理员审核评价，可以设置为通过、拒绝或发布状态
     * 需要管理员权限认证
     *
     * @param testimonialId 评价ID，UUID格式
     * @param request 状态变更请求参数
     * @return 更新后的评价详情信息
     */
    @PutMapping("/{testimonialId}/status")
    public ApiResponse<AdminTestimonialDTO> changeTestimonialStatus(@PathVariable String testimonialId,
                                                                  @Valid @RequestBody ChangeStatusRequest request) {
        AdminTestimonialDTO testimonial = adminTestimonialAppService.changeTestimonialStatus(testimonialId, request);
        return ApiResponse.success("状态修改成功", testimonial);
    }

    /**
     * 删除评价
     * 管理员删除不当或违规的评价
     * 需要管理员权限认证
     *
     * @param testimonialId 评价ID，UUID格式
     * @return 删除操作结果
     */
    @DeleteMapping("/{testimonialId}")
    public ApiResponse<Void> deleteTestimonial(@PathVariable String testimonialId) {
        adminTestimonialAppService.deleteTestimonial(testimonialId);
        return ApiResponse.success("删除成功");
    }

    /**
     * 设置评价排序权重
     * 管理员设置优质评价的排序权重，实现推荐置顶功能
     * 需要管理员权限认证
     *
     * @param testimonialId 评价ID，UUID格式
     * @param sortOrder 排序权重，数值越大排序越靠前
     * @return 更新后的评价详情信息
     */
    @PutMapping("/{testimonialId}/sort-order")
    public ApiResponse<AdminTestimonialDTO> updateSortOrder(@PathVariable String testimonialId,
                                                           @RequestParam Integer sortOrder) {
        AdminTestimonialDTO testimonial = adminTestimonialAppService.updateSortOrder(testimonialId, sortOrder);
        return ApiResponse.success("排序权重设置成功", testimonial);
    }
}