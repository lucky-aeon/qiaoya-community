package org.xhy.community.interfaces.interview.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.interview.dto.InterviewQuestionDTO;
import org.xhy.community.application.interview.service.AdminInterviewQuestionAppService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest;
import org.xhy.community.interfaces.interview.request.UpdateInterviewQuestionRequest;

/**
 * 管理员面试题管理控制器
 * 提供管理员对所有面试题的完整管理功能,包括查询、编辑、发布、归档、删除等操作
 * @module 面试题管理
 */
@RestController
@RequestMapping("/api/admin/interview-questions")
public class AdminInterviewQuestionController {

    private final AdminInterviewQuestionAppService adminInterviewQuestionAppService;

    public AdminInterviewQuestionController(AdminInterviewQuestionAppService adminInterviewQuestionAppService) {
        this.adminInterviewQuestionAppService = adminInterviewQuestionAppService;
    }

    /**
     * 获取面试题详情
     * 管理员查看任意面试题的详细信息
     * 需要管理员权限认证
     *
     * @param id 面试题ID
     * @return 面试题详情
     */
    @GetMapping("/{id}")
    public ApiResponse<InterviewQuestionDTO> getQuestion(@PathVariable String id) {
        InterviewQuestionDTO question = adminInterviewQuestionAppService.getById(id);
        return ApiResponse.success(question);
    }

    /**
     * 分页查询面试题列表
     * 管理员分页查看所有面试题,支持按状态和分类筛选
     * 需要管理员权限认证
     *
     * @param request 查询请求参数,包含分页和筛选条件
     * @return 分页的面试题列表数据
     */
    @GetMapping
    public ApiResponse<IPage<InterviewQuestionDTO>> getQuestions(InterviewQuestionQueryRequest request) {
        IPage<InterviewQuestionDTO> questions = adminInterviewQuestionAppService.queryQuestions(request);
        return ApiResponse.success(questions);
    }

    /**
     * 更新面试题
     * 管理员修改任意面试题的信息
     * 需要管理员权限认证
     *
     * @param id      面试题ID
     * @param request 更新面试题请求参数
     * @return 更新后的面试题详情
     */
    @PutMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_INTERVIEW_QUESTION_UPDATE)
    public ApiResponse<InterviewQuestionDTO> updateQuestion(@PathVariable String id,
                                                            @Valid @RequestBody UpdateInterviewQuestionRequest request) {
        InterviewQuestionDTO question = adminInterviewQuestionAppService.update(id, request);
        return ApiResponse.success("更新成功", question);
    }

    /**
     * 发布面试题
     * 管理员发布任意面试题
     * 需要管理员权限认证
     *
     * @param id 面试题ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    @ActivityLog(ActivityType.ADMIN_INTERVIEW_QUESTION_PUBLISH)
    public ApiResponse<Void> publishQuestion(@PathVariable String id) {
        adminInterviewQuestionAppService.publish(id);
        return ApiResponse.success("发布成功");
    }

    /**
     * 归档面试题
     * 管理员归档任意面试题
     * 需要管理员权限认证
     *
     * @param id 面试题ID
     * @return 操作结果
     */
    @PostMapping("/{id}/archive")
    @ActivityLog(ActivityType.ADMIN_INTERVIEW_QUESTION_ARCHIVE)
    public ApiResponse<Void> archiveQuestion(@PathVariable String id) {
        adminInterviewQuestionAppService.archive(id);
        return ApiResponse.success("归档成功");
    }

    /**
     * 删除面试题
     * 管理员软删除指定的面试题
     * 需要管理员权限认证
     *
     * @param id 面试题ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_INTERVIEW_QUESTION_DELETE)
    public ApiResponse<Void> deleteQuestion(@PathVariable String id) {
        adminInterviewQuestionAppService.delete(id);
        return ApiResponse.success("删除成功");
    }
}
