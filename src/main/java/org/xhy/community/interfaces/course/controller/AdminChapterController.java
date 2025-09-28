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
import org.xhy.community.interfaces.course.request.BatchUpdateChapterOrderRequest;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

import java.util.List;

/**
 * 管理员课程章节管理控制器
 * 提供管理员对课程章节的完整管理功能，包括创建、编辑、删除、查询等操作
 * @module 课程管理
 */
@RestController
@RequestMapping("/api/admin/chapters")
public class AdminChapterController {
    
    private final AdminChapterAppService adminChapterAppService;
    
    public AdminChapterController(AdminChapterAppService adminChapterAppService) {
        this.adminChapterAppService = adminChapterAppService;
    }
    
    /**
     * 创建课程章节
     * 管理员为指定课程创建新的章节内容
     * 需要管理员权限认证
     * 
     * @param request 创建章节请求参数，包含章节基本信息
     * @return 创建成功的章节详情信息
     */
    @PostMapping
    @ActivityLog(ActivityType.ADMIN_CHAPTER_CREATE)
    public ApiResponse<ChapterDTO> createChapter(@Valid @RequestBody CreateChapterRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        ChapterDTO chapter = adminChapterAppService.createChapter(request, currentUserId);
        return ApiResponse.success("创建成功",chapter);
    }
    
    /**
     * 更新课程章节
     * 管理员修改指定章节的信息内容
     * 需要管理员权限认证
     * 
     * @param id 章节ID，UUID格式
     * @param request 更新章节请求参数
     * @return 更新后的章节详情信息
     */
    @PutMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_CHAPTER_UPDATE)
    public ApiResponse<ChapterDTO> updateChapter(@PathVariable String id,
                                                @Valid @RequestBody UpdateChapterRequest request) {
        ChapterDTO chapter = adminChapterAppService.updateChapter(id, request);
        return ApiResponse.success("更新成功",chapter);
    }
    
    /**
     * 删除课程章节
     * 管理员软删除指定的课程章节
     * 需要管理员权限认证
     * 
     * @param id 章节ID，UUID格式
     * @return 删除操作结果
     */
    @DeleteMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_CHAPTER_DELETE)
    public ApiResponse<Void> deleteChapter(@PathVariable String id) {
        adminChapterAppService.deleteChapter(id);
        return ApiResponse.success("删除成功");
    }
    
    /**
     * 获取章节详情
     * 管理员查看指定章节的详细信息
     * 需要管理员权限认证
     * 
     * @param id 章节ID，UUID格式
     * @return 章节详情信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ChapterDTO> getChapter(@PathVariable String id) {
        ChapterDTO chapter = adminChapterAppService.getChapterById(id);
        return ApiResponse.success(chapter);
    }
    
    /**
     * 获取课程的所有章节
     * 管理员查看指定课程下的所有章节列表
     * 需要管理员权限认证
     * 
     * @param courseId 课程ID，UUID格式
     * @return 该课程下的所有章节列表
     */
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<ChapterDTO>> getChaptersByCourse(@PathVariable String courseId) {
        List<ChapterDTO> chapters = adminChapterAppService.getChaptersByCourseId(courseId);
        return ApiResponse.success(chapters);
    }
    
    /**
     * 分页查询章节列表
     * 管理员分页查看所有章节，支持多条件筛选
     * 需要管理员权限认证
     * 
     * @param request 章节查询请求参数，包含分页和筛选条件
     * @return 分页的章节列表数据
     */
    @GetMapping
    public ApiResponse<IPage<ChapterDTO>> getChapters(ChapterQueryRequest request) {
        IPage<ChapterDTO> chapters = adminChapterAppService.getPagedChapters(request);
        return ApiResponse.success(chapters);
    }
    
    /**
     * 批量更新章节顺序
     * 管理员通过拖拽调整章节顺序，前端传递排好序的章节ID列表
     * 后端根据数组顺序设置sortOrder，按从大到小存储
     * 需要管理员权限认证
     * 
     * @param request 批量更新章节顺序请求参数，包含排好序的章节ID列表
     * @return 更新操作结果
     */
    @PutMapping("/order")
    @ActivityLog(ActivityType.ADMIN_CHAPTER_REORDER)
    public ApiResponse<Void> batchUpdateChapterOrder(@Valid @RequestBody BatchUpdateChapterOrderRequest request) {
        adminChapterAppService.batchUpdateChapterOrder(request);
        return ApiResponse.success("更新章节顺序成功");
    }
}
