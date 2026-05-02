package org.xhy.community.interfaces.course.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.AdminChapterTranscriptDTO;
import org.xhy.community.application.course.service.AdminChapterTranscriptAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminChapterTranscriptController {

    private final AdminChapterTranscriptAppService appService;

    public AdminChapterTranscriptController(AdminChapterTranscriptAppService appService) {
        this.appService = appService;
    }

    @GetMapping("/chapters/{chapterId}/transcript")
    public ApiResponse<AdminChapterTranscriptDTO> getTranscript(@PathVariable String chapterId) {
        return ApiResponse.success(appService.getTranscript(chapterId));
    }

    @PostMapping("/chapters/{chapterId}/transcript/retry")
    public ApiResponse<AdminChapterTranscriptDTO> retry(@PathVariable String chapterId) {
        return ApiResponse.success("已重新进入队列", appService.retry(chapterId));
    }

    @PostMapping("/chapters/{chapterId}/transcript/regenerate")
    public ApiResponse<AdminChapterTranscriptDTO> regenerate(@PathVariable String chapterId) {
        AdminChapterTranscriptDTO transcript = appService.regenerate(chapterId);
        String message = "NOT_GENERATED".equals(transcript.getStatus()) && transcript.getErrorCode() != null
                ? "未创建转写任务"
                : "已重新生成";
        return ApiResponse.success(message, transcript);
    }

    @PostMapping("/courses/{courseId}/transcripts/batch")
    public ApiResponse<Map<String, Integer>> batchGenerate(@PathVariable String courseId) {
        int count = appService.batchGenerateForCourse(courseId);
        return ApiResponse.success("已创建批量生成任务", Map.of("count", count));
    }
}
