package org.xhy.community.interfaces.post.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.CategoryTreeDTO;
import org.xhy.community.application.post.service.AppCategoryAppService;
import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

/**
 * 用户前台分类控制器
 * 提供面向应用端用户的分类查询功能
 * @module 用户前台API
 */
@RestController
@RequestMapping("/api/app/categories")
public class AppCategoryController {
    
    private final AppCategoryAppService appCategoryAppService;
    
    public AppCategoryController(AppCategoryAppService appCategoryAppService) {
        this.appCategoryAppService = appCategoryAppService;
    }
    
    /**
     * 获取分类树形结构
     * 获取所有激活的分类，以树形结构返回
     * 
     * @param type 分类类型过滤（可选），可选值：
     *             ARTICLE: 文章类型
     *             QA: 问答类型
     * @return 树形结构的分类列表，包含父子关系
     */
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeDTO>> getCategoryTree(@RequestParam(required = false) CategoryType type) {
        List<CategoryTreeDTO> categoryTree = appCategoryAppService.getCategoryTree(type);
        return ApiResponse.success(categoryTree);
    }
}