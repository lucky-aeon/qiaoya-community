package org.xhy.community.application.expression.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.expression.dto.ExpressionDTO;
import org.xhy.community.domain.expression.entity.ExpressionTypeEntity;
import org.xhy.community.interfaces.expression.request.CreateExpressionRequest;
import org.xhy.community.interfaces.expression.request.UpdateExpressionRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ExpressionAssembler {

    public static ExpressionDTO toDTO(ExpressionTypeEntity entity) {
        if (entity == null) return null;
        ExpressionDTO dto = new ExpressionDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static List<ExpressionDTO> toDTOList(List<ExpressionTypeEntity> entities) {
        if (entities == null) return null;
        return entities.stream().map(ExpressionAssembler::toDTO).collect(Collectors.toList());
        
    }

    public static ExpressionTypeEntity fromCreateRequest(CreateExpressionRequest request) {
        ExpressionTypeEntity entity = new ExpressionTypeEntity();
        BeanUtils.copyProperties(request, entity);
        return entity;
    }

    public static ExpressionTypeEntity fromUpdateRequest(UpdateExpressionRequest request, String id) {
        ExpressionTypeEntity entity = new ExpressionTypeEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        return entity;
    }
}

