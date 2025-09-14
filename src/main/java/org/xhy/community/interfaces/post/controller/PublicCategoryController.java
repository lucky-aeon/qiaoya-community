package org.xhy.community.interfaces.post.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.post.dto.CategoryTreeDTO;
import org.xhy.community.application.post.service.PublicCategoryAppService;
import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

/**
 * 公开分类查询控制器
 * 提供公开的分类查询功能，无需登录认证
 * @module 公开API
 */
@RestController
@RequestMapping("/api/public/categories")
public class PublicCategoryController {
    
    private final PublicCategoryAppService publicCategoryAppService;
    
    public PublicCategoryController(PublicCategoryAppService publicCategoryAppService) {
        this.publicCategoryAppService = publicCategoryAppService;
    }
    
    /**
     * 获取分类树形结构
     * 获取所有激活的分类，以树形结构返回
     * 无需认证，公开访问
     * 
     * @param type 分类类型过滤（可选），可选值：
     *             ARTICLE: 文章类型
     *             QA: 问答类型
     * @return 树形结构的分类列表，包含父子关系
     */
    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeDTO>> getCategoryTree(@RequestParam(required = false) CategoryType type) {
        List<CategoryTreeDTO> categoryTree = publicCategoryAppService.getCategoryTree(type);
        return ApiResponse.success(categoryTree);
    }
}