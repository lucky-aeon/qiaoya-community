package org.xhy.community.domain.expression.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.expression.entity.ExpressionTypeEntity;
import org.xhy.community.domain.expression.entity.ReactionEntity;
import org.xhy.community.domain.expression.repository.ExpressionTypeRepository;
import org.xhy.community.domain.expression.repository.ReactionRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ExpressionErrorCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpressionDomainService {

    private final ExpressionTypeRepository expressionTypeRepository;
    private final ReactionRepository reactionRepository;

    public ExpressionDomainService(ExpressionTypeRepository expressionTypeRepository,
                                   ReactionRepository reactionRepository) {
        this.expressionTypeRepository = expressionTypeRepository;
        this.reactionRepository = reactionRepository;
    }

    // 创建表情类型（校验 code 唯一）
    public ExpressionTypeEntity create(ExpressionTypeEntity entity) {
        if (isCodeExists(entity.getCode(), null)) {
            org.slf4j.LoggerFactory.getLogger(ExpressionDomainService.class)
                    .warn("【表情类型】创建失败：code 重复，code={}", entity.getCode());
            throw new BusinessException(ExpressionErrorCode.EXPRESSION_CODE_EXISTS);
        }
        expressionTypeRepository.insert(entity);
        return entity;
    }

    // 更新表情类型（若修改 code 需校验唯一）
    public ExpressionTypeEntity update(ExpressionTypeEntity patch) {
        ExpressionTypeEntity existing = getById(patch.getId());

        if (patch.getCode() != null && !patch.getCode().equals(existing.getCode())) {
            if (isCodeExists(patch.getCode(), existing.getId())) {
                org.slf4j.LoggerFactory.getLogger(ExpressionDomainService.class)
                        .warn("【表情类型】更新失败：code 重复，id={}, code={}", existing.getId(), patch.getCode());
                throw new BusinessException(ExpressionErrorCode.EXPRESSION_CODE_EXISTS);
            }
            existing.setCode(patch.getCode());
        }
        if (patch.getName() != null) {
            existing.setName(patch.getName());
        }
        if (patch.getImageUrl() != null) {
            existing.setImageUrl(patch.getImageUrl());
        }
        if (patch.getSortOrder() != null) {
            existing.setSortOrder(patch.getSortOrder());
        }
        if (patch.getIsActive() != null) {
            existing.setIsActive(patch.getIsActive());
        }
        expressionTypeRepository.updateById(existing);
        return existing;
    }

    // 删除表情类型（在用校验）
    public void delete(String id) {
        ExpressionTypeEntity existing = getById(id);

        boolean inUse = reactionRepository.exists(new LambdaQueryWrapper<ReactionEntity>()
                .eq(ReactionEntity::getReactionType, existing.getCode()));
        if (inUse) {
            org.slf4j.LoggerFactory.getLogger(ExpressionDomainService.class)
                    .warn("【表情类型】删除失败：仍在使用，id={}", id);
            throw new BusinessException(ExpressionErrorCode.EXPRESSION_IN_USE);
        }
        expressionTypeRepository.deleteById(id);
    }

    // 启停切换，返回最新状态
    public boolean toggle(String id) {
        ExpressionTypeEntity existing = getById(id);
        existing.setIsActive(existing.getIsActive() == null ? Boolean.TRUE : !existing.getIsActive());
        expressionTypeRepository.updateById(existing);
        return existing.getIsActive();
    }

    // 管理端分页
    public IPage<ExpressionTypeEntity> page(Integer pageNum, Integer pageSize, String code, String name, Boolean isActive) {
        Page<ExpressionTypeEntity> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<ExpressionTypeEntity> qw = new LambdaQueryWrapper<ExpressionTypeEntity>()
                .like(StringUtils.hasText(code), ExpressionTypeEntity::getCode, code)
                .like(StringUtils.hasText(name), ExpressionTypeEntity::getName, name)
                .eq(isActive != null, ExpressionTypeEntity::getIsActive, isActive)
                .orderByAsc(ExpressionTypeEntity::getSortOrder)
                .orderByAsc(ExpressionTypeEntity::getId);
        return expressionTypeRepository.selectPage(page, qw);
    }

    // 前台：获取启用的表情列表
    public List<ExpressionTypeEntity> listEnabled() {
        LambdaQueryWrapper<ExpressionTypeEntity> qw = new LambdaQueryWrapper<ExpressionTypeEntity>()
                .eq(ExpressionTypeEntity::getIsActive, true)
                .orderByAsc(ExpressionTypeEntity::getSortOrder)
                .orderByAsc(ExpressionTypeEntity::getId);
        return expressionTypeRepository.selectList(qw);
    }

    // 前台：alias 映射（code -> imageUrl）
    public Map<String, String> getAliasMap() {
        return listEnabled().stream().collect(Collectors.toMap(
                ExpressionTypeEntity::getCode,
                ExpressionTypeEntity::getImageUrl
        ));
    }

    public ExpressionTypeEntity getById(String id) {
        ExpressionTypeEntity entity = expressionTypeRepository.selectOne(
                new LambdaQueryWrapper<ExpressionTypeEntity>().eq(ExpressionTypeEntity::getId, id)
        );
        if (entity == null) {
            org.slf4j.LoggerFactory.getLogger(ExpressionDomainService.class)
                    .warn("【表情类型】未找到：id={}", id);
            throw new BusinessException(ExpressionErrorCode.EXPRESSION_NOT_FOUND);
        }
        return entity;
    }

    public boolean isCodeExists(String code, String excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<ExpressionTypeEntity> qw = new LambdaQueryWrapper<ExpressionTypeEntity>()
                .eq(ExpressionTypeEntity::getCode, code)
                .ne(StringUtils.hasText(excludeId), ExpressionTypeEntity::getId, excludeId);
        return expressionTypeRepository.exists(qw);
    }
}
