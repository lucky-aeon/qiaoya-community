package org.xhy.community.interfaces.expression.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.expression.dto.ExpressionDTO;
import org.xhy.community.application.expression.service.ExpressionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

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

    /** 获取 Markdown 别名映射（需登录） */
    @GetMapping("/alias-map")
    public ApiResponse<Map<String, String>> getAliasMap() {
        Map<String, String> map = expressionAppService.getAliasMap();
        return ApiResponse.success(map);
    }

    /** 获取启用的表情列表（需登录） */
    @GetMapping
    public ApiResponse<List<ExpressionDTO>> listEnabled() {
        List<ExpressionDTO> list = expressionAppService.listEnabled();
        return ApiResponse.success(list);
    }
}

