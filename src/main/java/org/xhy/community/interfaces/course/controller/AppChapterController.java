package org.xhy.community.interfaces.course.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.course.dto.FrontChapterDetailDTO;
import org.xhy.community.application.course.service.ChapterAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

/**
 * 前台章节控制器
 * 提供面向前台用户的章节查询功能
 * @module 前台API
 */
@RestController
@RequestMapping("/api/app/chapters")
public class AppChapterController {

    private final ChapterAppService chapterAppService;

    public AppChapterController(ChapterAppService chapterAppService) {
        this.chapterAppService = chapterAppService;
    }

    /**
     * 根据章节ID获取章节详情
     * 获取指定章节的详细信息，包含章节内容和课程名称
     *
     * @param id 章节ID，UUID格式
     * @return 章节详细信息，包含：
     *         - 章节基本信息（标题、内容、排序、阅读时长等）
     *         - 课程ID和课程名称
     *         - 创建和更新时间
     */
    @GetMapping("/{id}")
    public ApiResponse<FrontChapterDetailDTO> getChapterDetail(@PathVariable String id) {
        FrontChapterDetailDTO chapterDetail = chapterAppService.getChapterById(id);
        return ApiResponse.success(chapterDetail);
    }
}