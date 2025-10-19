package org.xhy.community.interfaces.interview.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.interview.dto.InterviewQuestionDTO;
import org.xhy.community.application.interview.service.InterviewQuestionAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.interview.request.CreateInterviewQuestionRequest;
import org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest;
import org.xhy.community.interfaces.interview.request.UpdateInterviewQuestionRequest;

/**
 * 面试题控制器
 * 提供用户对面试题的创建、修改、查询、发布、归档、删除等功能
 * @module 面试题管理
 */
@RestController
@RequestMapping("/api/interview-questions")
public class InterviewQuestionController {

    private final InterviewQuestionAppService interviewQuestionAppService;

    public InterviewQuestionController(InterviewQuestionAppService interviewQuestionAppService) {
        this.interviewQuestionAppService = interviewQuestionAppService;
    }

    /**
     * 创建面试题
     * 用户创建新的面试题，默认为草稿状态
     * 需要用户登录
     *
     * @param request 创建面试题请求参数
     * @return 创建成功的面试题详情
     */
    @PostMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_CREATE", name = "创建面试题")})
    public ApiResponse<InterviewQuestionDTO> createQuestion(@Valid @RequestBody CreateInterviewQuestionRequest request) {
        String userId = UserContext.getCurrentUserId();
        InterviewQuestionDTO question = interviewQuestionAppService.createQuestion(request, userId);
        return ApiResponse.success("创建成功", question);
    }

    /**
     * 更新面试题
     * 用户修改自己创建的面试题
     * 需要用户登录,只能修改自己的面试题
     *
     * @param id      面试题ID
     * @param request 更新面试题请求参数
     * @return 更新后的面试题详情
     */
    @PutMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_UPDATE", name = "更新面试题")})
    public ApiResponse<InterviewQuestionDTO> updateQuestion(@PathVariable String id,
                                                            @Valid @RequestBody UpdateInterviewQuestionRequest request) {
        String userId = UserContext.getCurrentUserId();
        InterviewQuestionDTO question = interviewQuestionAppService.updateQuestion(id, request, userId);
        return ApiResponse.success("更新成功", question);
    }

    /**
     * 获取面试题详情
     * 查看指定面试题的详细信息
     *
     * @param id 面试题ID
     * @return 面试题详情
     */
    @GetMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_DETAIL", name = "查看面试题详情")})
    public ApiResponse<InterviewQuestionDTO> getQuestion(@PathVariable String id) {
        InterviewQuestionDTO question = interviewQuestionAppService.getById(id);
        return ApiResponse.success(question);
    }

    /**
     * 分页查询公开题库
     * 查询所有已发布的面试题,供所有用户浏览
     * 不需要登录,按发布时间倒序排列
     *
     * @param request 查询请求参数,支持按分类筛选
     * @return 分页的公开面试题列表
     */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_PUBLIC_LIST", name = "公开题库列表")})
    public ApiResponse<IPage<InterviewQuestionDTO>> getPublicQuestions(InterviewQuestionQueryRequest request) {
        IPage<InterviewQuestionDTO> questions = interviewQuestionAppService.queryPublicQuestions(request);
        return ApiResponse.success(questions);
    }

    /**
     * 分页查询我的面试题
     * 用户查询自己创建的面试题列表,支持按状态和分类筛选
     * 需要用户登录
     *
     * @param request 查询请求参数
     * @return 分页的面试题列表
     */
    @GetMapping("/my")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_MY_LIST", name = "我的面试题列表")})
    public ApiResponse<IPage<InterviewQuestionDTO>> getMyQuestions(InterviewQuestionQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<InterviewQuestionDTO> questions = interviewQuestionAppService.queryQuestions(request, userId);
        return ApiResponse.success(questions);
    }

    /**
     * 发布面试题
     * 将草稿状态的面试题发布
     * 需要用户登录,只能发布自己的面试题
     *
     * @param id 面试题ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_PUBLISH", name = "发布面试题")})
    public ApiResponse<Void> publishQuestion(@PathVariable String id) {
        String userId = UserContext.getCurrentUserId();
        interviewQuestionAppService.publish(id, userId);
        return ApiResponse.success("发布成功");
    }

    /**
     * 归档面试题
     * 将面试题归档
     * 需要用户登录,只能归档自己的面试题
     *
     * @param id 面试题ID
     * @return 操作结果
     */
    @PostMapping("/{id}/archive")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_ARCHIVE", name = "归档面试题")})
    public ApiResponse<Void> archiveQuestion(@PathVariable String id) {
        String userId = UserContext.getCurrentUserId();
        interviewQuestionAppService.archive(id, userId);
        return ApiResponse.success("归档成功");
    }

    /**
     * 删除面试题
     * 软删除指定的面试题
     * 需要用户登录,只能删除自己的面试题
     *
     * @param id 面试题ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "INTERVIEW_QUESTION_DELETE", name = "删除面试题")})
    public ApiResponse<Void> deleteQuestion(@PathVariable String id) {
        String userId = UserContext.getCurrentUserId();
        interviewQuestionAppService.delete(id, userId);
        return ApiResponse.success("删除成功");
    }
}
