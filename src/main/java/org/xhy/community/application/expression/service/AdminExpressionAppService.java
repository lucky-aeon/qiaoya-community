package org.xhy.community.application.expression.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.expression.assembler.ExpressionAssembler;
import org.xhy.community.application.expression.dto.ExpressionDTO;
import org.xhy.community.domain.expression.entity.ExpressionTypeEntity;
import org.xhy.community.domain.expression.service.ExpressionDomainService;
import org.xhy.community.interfaces.expression.request.CreateExpressionRequest;
import org.xhy.community.interfaces.expression.request.ExpressionQueryRequest;
import org.xhy.community.interfaces.expression.request.UpdateExpressionRequest;

@Service
public class AdminExpressionAppService {

    private final ExpressionDomainService expressionDomainService;

    public AdminExpressionAppService(ExpressionDomainService expressionDomainService) {
        this.expressionDomainService = expressionDomainService;
    }

    public ExpressionDTO create(CreateExpressionRequest request) {
        ExpressionTypeEntity entity = ExpressionAssembler.fromCreateRequest(request);
        ExpressionTypeEntity created = expressionDomainService.create(entity);
        return ExpressionAssembler.toDTO(created);
    }

    public ExpressionDTO update(String id, UpdateExpressionRequest request) {
        ExpressionTypeEntity patch = ExpressionAssembler.fromUpdateRequest(request, id);
        ExpressionTypeEntity updated = expressionDomainService.update(patch);
        return ExpressionAssembler.toDTO(updated);
    }

    public void delete(String id) {
        expressionDomainService.delete(id);
    }

    public boolean toggle(String id) {
        return expressionDomainService.toggle(id);
    }

    public IPage<ExpressionDTO> page(ExpressionQueryRequest request) {
        IPage<ExpressionTypeEntity> page = expressionDomainService.page(
                request.getPageNum(),
                request.getPageSize(),
                request.getCode(),
                request.getName(),
                request.getIsActive()
        );
        return page.convert(ExpressionAssembler::toDTO);
    }
}

