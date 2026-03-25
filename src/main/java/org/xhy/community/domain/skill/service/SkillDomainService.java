package org.xhy.community.domain.skill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.skill.repository.SkillRepository;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;

@Service
public class SkillDomainService {

    private final SkillRepository skillRepository;

    public SkillDomainService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public SkillEntity createSkill(SkillEntity skill) {
        skillRepository.insert(skill);
        return skill;
    }

    public SkillEntity updateSkill(String skillId, SkillEntity updatedSkill, String currentUserId) {
        SkillEntity existingSkill = getSkillById(skillId);
        validateAuthor(existingSkill, currentUserId);
        existingSkill.updateContent(
                updatedSkill.getName(),
                updatedSkill.getSummary(),
                updatedSkill.getDescription(),
                updatedSkill.getGithubUrl()
        );
        skillRepository.updateById(existingSkill);
        return existingSkill;
    }

    public SkillEntity getSkillById(String skillId) {
        SkillEntity skill = skillRepository.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "技能不存在");
        }
        return skill;
    }

    public SkillEntity getUserSkillById(String skillId, String currentUserId) {
        SkillEntity skill = getSkillById(skillId);
        validateAuthor(skill, currentUserId);
        return skill;
    }

    public IPage<SkillEntity> querySkills(SkillQuery query) {
        Page<SkillEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<SkillEntity> queryWrapper = new LambdaQueryWrapper<SkillEntity>()
                .eq(StringUtils.hasText(query.getUserId()), SkillEntity::getUserId, query.getUserId())
                .and(StringUtils.hasText(query.getKeyword()), wrapper -> wrapper
                        .like(SkillEntity::getName, query.getKeyword())
                        .or()
                        .like(SkillEntity::getSummary, query.getKeyword()))
                .orderByDesc(SkillEntity::getCreateTime);

        return skillRepository.selectPage(page, queryWrapper);
    }

    public void deleteSkill(String skillId, String currentUserId) {
        SkillEntity skill = getSkillById(skillId);
        validateAuthor(skill, currentUserId);
        skillRepository.deleteById(skillId);
    }

    public Long countSkills() {
        return skillRepository.selectCount(null);
    }

    private void validateAuthor(SkillEntity skill, String currentUserId) {
        if (!skill.getUserId().equals(currentUserId)) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "只能操作自己的技能");
        }
    }
}
