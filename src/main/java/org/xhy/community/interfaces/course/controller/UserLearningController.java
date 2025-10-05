package org.xhy.community.interfaces.course.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.CourseProgressDTO;
import org.xhy.community.application.course.service.CourseProgressAppService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.xhy.community.application.course.dto.LearningRecordItemDTO;
import org.xhy.community.interfaces.course.request.LearningRecordQueryRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.course.request.ReportChapterProgressRequest;

@RestController
@RequestMapping("/api/user/learning")
public class UserLearningController {

    private final CourseProgressAppService courseProgressAppService;

    public UserLearningController(CourseProgressAppService courseProgressAppService) {
        this.courseProgressAppService = courseProgressAppService;
    }

    /**
     * 上报章节学习进度（心跳/阈值打点）
     */
    @PostMapping("/progress/report")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LEARNING_PROGRESS_REPORT", name = "上报学习进度")})
    public ApiResponse<CourseProgressDTO> report(@Valid @RequestBody ReportChapterProgressRequest request) {
        String userId = UserContext.getCurrentUserId();
        CourseProgressDTO dto = courseProgressAppService.reportProgress(userId, request);
        return ApiResponse.success(dto);
    }

    /**
     * 获取当前用户某课程的学习进度汇总
     */
    @GetMapping("/progress/{courseId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LEARNING_PROGRESS_VIEW", name = "查看学习进度")})
    public ApiResponse<CourseProgressDTO> getCourseProgress(@PathVariable String courseId) {
        String userId = UserContext.getCurrentUserId();
        CourseProgressDTO dto = courseProgressAppService.getCourseProgress(userId, courseId);
        return ApiResponse.success(dto);
    }


    /** 学习记录分页：全部课程的进度与最近学习位置 */
    @GetMapping("/records")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "LEARNING_PROGRESS_VIEW", name = "查看学习进度")})
    public ApiResponse<IPage<LearningRecordItemDTO>> listMyLearningRecords(@Valid LearningRecordQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<LearningRecordItemDTO> page = courseProgressAppService.listMyLearningRecords(userId, request.getPageNum(), request.getPageSize());
        return ApiResponse.success(page);
    }
}
