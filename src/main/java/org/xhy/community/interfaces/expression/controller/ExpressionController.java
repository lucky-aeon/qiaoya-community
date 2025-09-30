package org.xhy.community.interfaces.expression.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.expression.dto.ExpressionDTO;
import org.xhy.community.application.expression.service.ExpressionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.List;
import java.util.Map;

/**
 * 表情（非管理员）接口，需登录访问
 */
@RestController
@RequestMapping("/api/expressions")
public class ExpressionController {

    private final ExpressionAppService expressionAppService;

    public ExpressionController(ExpressionAppService expressionAppService) {
        this.expressionAppService = expressionAppService;
    }

    /** 获取 Markdown 别名映射（需功能权限） */
    @GetMapping("/alias-map")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "EXPRESSION_ALIAS_MAP", name = "获取表情映射")})
    public ApiResponse<Map<String, String>> getAliasMap() {
        Map<String, String> map = expressionAppService.getAliasMap();
        return ApiResponse.success(map);
    }

    /** 获取启用的表情列表（需功能权限） */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "EXPRESSION_LIST", name = "查看表情列表")})
    public ApiResponse<List<ExpressionDTO>> listEnabled() {
        List<ExpressionDTO> list = expressionAppService.listEnabled();
        return ApiResponse.success(list);
    }
}
