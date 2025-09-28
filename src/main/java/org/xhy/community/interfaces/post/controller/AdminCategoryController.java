package org.xhy.community.interfaces.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.CategoryDTO;
import org.xhy.community.application.post.service.AdminCategoryAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.post.request.CreateCategoryRequest;
import org.xhy.community.interfaces.post.request.UpdateCategoryRequest;
import org.xhy.community.interfaces.post.request.CategoryQueryRequest;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

/**
 * 管理员分类管理控制器
 * 提供管理员对分类的完整管理功能，包括创建、编辑、删除、查询等操作
 * @module 分类管理
 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    
    private final AdminCategoryAppService adminCategoryAppService;
    
    public AdminCategoryController(AdminCategoryAppService adminCategoryAppService) {
        this.adminCategoryAppService = adminCategoryAppService;
    }
    
    /**
     * 创建分类
     * 管理员创建新的分类（支持主分类和子分类）
     * 需要管理员权限认证
     * 
     * @param request 创建分类请求参数，包含分类基本信息
     * @return 创建成功的分类详情信息
     */
    @PostMapping
    @ActivityLog(ActivityType.ADMIN_CATEGORY_CREATE)
    public ApiResponse<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryDTO category = adminCategoryAppService.createCategory(request);
        return ApiResponse.success(category);
    }
    
    /**
     * 更新分类
     * 管理员修改指定分类的信息内容
     * 需要管理员权限认证
     * 
     * @param id 分类ID，UUID格式
     * @param request 更新分类请求参数
     * @return 更新后的分类详情信息
     */
    @PutMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_CATEGORY_UPDATE)
    public ApiResponse<CategoryDTO> updateCategory(@PathVariable String id,
                                                  @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryDTO category = adminCategoryAppService.updateCategory(id, request);
        return ApiResponse.success(category);
    }
    
    /**
     * 删除分类
     * 管理员软删除指定的分类
     * 需要管理员权限认证
     * 
     * @param id 分类ID，UUID格式
     * @return 删除操作结果
     */
    @DeleteMapping("/{id}")
    @ActivityLog(ActivityType.ADMIN_CATEGORY_DELETE)
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        adminCategoryAppService.deleteCategory(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取分类详情
     * 管理员查看指定分类的详细信息
     * 需要管理员权限认证
     * 
     * @param id 分类ID，UUID格式
     * @return 分类详情信息
     */
    @GetMapping("/{id}")
    public ApiResponse<CategoryDTO> getCategory(@PathVariable String id) {
        CategoryDTO category = adminCategoryAppService.getCategoryById(id);
        return ApiResponse.success(category);
    }
    
    /**
     * 分页查询分类列表
     * 管理员分页查看所有分类，支持类型和父分类筛选
     * 需要管理员权限认证
     * 
     * @param request 分类查询请求参数，包含分页和筛选条件
     * @return 分页的分类列表数据
     */
    @GetMapping
    public ApiResponse<IPage<CategoryDTO>> getCategories(CategoryQueryRequest request) {
        IPage<CategoryDTO> categories = adminCategoryAppService.getPagedCategories(request);
        return ApiResponse.success(categories);
    }
}
