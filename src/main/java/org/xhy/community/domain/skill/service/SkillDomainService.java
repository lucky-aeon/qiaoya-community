package org.xhy.community.domain.skill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.skill.repository.SkillRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SkillErrorCode;

@Service
public class SkillDomainService {

    private final SkillRepository skillRepository;

    public SkillDomainService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public SkillEntity createSkill(SkillEntity entity) {
        skillRepository.insert(entity);
        return entity;
    }

    public SkillEntity updateSkill(SkillEntity updated, String currentUserId) {
        SkillEntity existing = getSkillById(updated.getId());
        ensureOwner(existing, currentUserId);
        existing.updateContent(updated.getName(), updated.getSummary(), updated.getDescription(), updated.getGithubUrl());
        skillRepository.updateById(existing);
        return existing;
    }

    public void deleteSkill(String skillId, String currentUserId) {
        SkillEntity existing = getSkillById(skillId);
        ensureOwner(existing, currentUserId);
        skillRepository.deleteById(skillId);
    }

    public IPage<SkillEntity> queryPublicSkills(SkillQuery query) {
        Page<SkillEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<SkillEntity>()
                .orderByDesc(SkillEntity::getCreateTime);
        return skillRepository.selectPage(page, wrapper);
    }

    public IPage<SkillEntity> queryUserSkills(String userId, SkillQuery query) {
        Page<SkillEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<SkillEntity>()
                .eq(SkillEntity::getUserId, userId)
                .orderByDesc(SkillEntity::getUpdateTime)
                .orderByDesc(SkillEntity::getCreateTime);
        return skillRepository.selectPage(page, wrapper);
    }

    private SkillEntity getSkillById(String skillId) {
        SkillEntity skill = skillRepository.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(SkillErrorCode.SKILL_NOT_FOUND);
        }
        return skill;
    }

    private void ensureOwner(SkillEntity skill, String currentUserId) {
        if (!skill.getUserId().equals(currentUserId)) {
            throw new BusinessException(SkillErrorCode.UNAUTHORIZED_MODIFY);
        }
    }
}
