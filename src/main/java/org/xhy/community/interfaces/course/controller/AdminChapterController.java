package org.xhy.community.interfaces.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.ChapterDTO;
import org.xhy.community.application.course.service.AdminChapterAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.course.request.CreateChapterRequest;
import org.xhy.community.interfaces.course.request.UpdateChapterRequest;
import org.xhy.community.interfaces.course.request.ChapterQueryRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chapters")
public class AdminChapterController {
    
    private final AdminChapterAppService adminChapterAppService;
    
    public AdminChapterController(AdminChapterAppService adminChapterAppService) {
        this.adminChapterAppService = adminChapterAppService;
    }
    
    @PostMapping
    public ApiResponse<ChapterDTO> createChapter(@Valid @RequestBody CreateChapterRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        ChapterDTO chapter = adminChapterAppService.createChapter(request, currentUserId);
        return ApiResponse.success(chapter);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<ChapterDTO> updateChapter(@PathVariable String id,
                                                @Valid @RequestBody UpdateChapterRequest request) {
        ChapterDTO chapter = adminChapterAppService.updateChapter(id, request);
        return ApiResponse.success(chapter);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChapter(@PathVariable String id) {
        adminChapterAppService.deleteChapter(id);
        return ApiResponse.success();
    }
    
    @GetMapping("/{id}")
    public ApiResponse<ChapterDTO> getChapter(@PathVariable String id) {
        ChapterDTO chapter = adminChapterAppService.getChapterById(id);
        return ApiResponse.success(chapter);
    }
    
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<ChapterDTO>> getChaptersByCourse(@PathVariable String courseId) {
        List<ChapterDTO> chapters = adminChapterAppService.getChaptersByCourseId(courseId);
        return ApiResponse.success(chapters);
    }
    
    @GetMapping
    public ApiResponse<IPage<ChapterDTO>> getChapters(ChapterQueryRequest request) {
        IPage<ChapterDTO> chapters = adminChapterAppService.getPagedChapters(request);
        return ApiResponse.success(chapters);
    }
}