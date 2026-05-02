package org.xhy.community.interfaces.course.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.FrontChapterDetailDTO;
import org.xhy.community.application.course.dto.ChapterTranscriptDTO;
import org.xhy.community.application.course.dto.LatestChapterDTO;
import org.xhy.community.application.course.service.ChapterAppService;
import org.xhy.community.application.course.service.ChapterTranscriptAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.List;

/**
 * 前台章节控制器
 * 提供面向前台用户的章节查询功能
 * @module 前台API
 */
@RestController
@RequestMapping("/api/app/chapters")
public class AppChapterController {

    private final ChapterAppService chapterAppService;
    private final ChapterTranscriptAppService chapterTranscriptAppService;

    public AppChapterController(ChapterAppService chapterAppService,
                                ChapterTranscriptAppService chapterTranscriptAppService) {
        this.chapterAppService = chapterAppService;
        this.chapterTranscriptAppService = chapterTranscriptAppService;
    }

    /**
     * 根据章节ID获取章节详情
     * 获取指定章节的详细信息，包含章节内容和课程名称
     * 需要验证用户是否有权限访问该章节（通过课程购买或套餐解锁）
     *
     * @param id 章节ID，UUID格式
     * @return 章节详细信息，包含：
     *         - 章节基本信息（标题、内容、排序、阅读时长等）
     *         - 课程ID和课程名称
     *         - 创建和更新时间
     */
    @GetMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CHAPTER_APP_DETAIL", name = "前台章节详情")})
    public ApiResponse<FrontChapterDetailDTO> getChapterDetail(@PathVariable String id) {
        String userId = UserContext.getCurrentUserId();
        FrontChapterDetailDTO chapterDetail = chapterAppService.getChapterById(id, userId);
        return ApiResponse.success(chapterDetail);
    }

    /**
     * 获取章节视频文字稿
     * 只返回用户有权限访问的章节文字稿；未生成时返回 NOT_GENERATED 状态。
     */
    @GetMapping("/{id}/transcript")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CHAPTER_APP_TRANSCRIPT", name = "前台章节视频文字稿")})
    public ApiResponse<ChapterTranscriptDTO> getChapterTranscript(@PathVariable String id) {
        String userId = UserContext.getCurrentUserId();
        return ApiResponse.success(chapterTranscriptAppService.getChapterTranscript(id, userId));
    }

    /**
     * 获取最新章节列表
     * 查询最新的5条课程章节，包含章节信息和课程名称
     *
     * @return 最新章节列表
     */
    @GetMapping("/latest")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CHAPTER_APP_LATEST", name = "最新章节列表")})
    public ApiResponse<List<LatestChapterDTO>> getLatestChapters() {
        List<LatestChapterDTO> chapters = chapterAppService.getLatestChapters();
        return ApiResponse.success(chapters);
    }
}
