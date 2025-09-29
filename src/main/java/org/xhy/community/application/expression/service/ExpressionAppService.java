package org.xhy.community.application.expression.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.expression.assembler.ExpressionAssembler;
import org.xhy.community.application.expression.dto.ExpressionDTO;
import org.xhy.community.domain.expression.entity.ExpressionTypeEntity;
import org.xhy.community.domain.expression.service.ExpressionDomainService;

import java.util.List;
import java.util.Map;

@Service
public class ExpressionAppService {

    private final ExpressionDomainService expressionDomainService;

    public ExpressionAppService(ExpressionDomainService expressionDomainService) {
        this.expressionDomainService = expressionDomainService;
    }

    // 前台：获取启用的表情列表
    public List<ExpressionDTO> listEnabled() {
        List<ExpressionTypeEntity> list = expressionDomainService.listEnabled();
        return ExpressionAssembler.toDTOList(list);
    }

    // 前台：获取 Markdown 别名映射
    public Map<String, String> getAliasMap() {
        return expressionDomainService.getAliasMap();
    }
}

