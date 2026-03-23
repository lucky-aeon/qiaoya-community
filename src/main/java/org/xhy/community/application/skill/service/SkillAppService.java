package org.xhy.community.application.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.skill.assembler.SkillAssembler;
import org.xhy.community.application.skill.dto.SkillDTO;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.skill.service.SkillDomainService;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;
import org.xhy.community.interfaces.skill.request.UpdateSkillRequest;

@Service
public class SkillAppService {

    private final SkillDomainService skillDomainService;

    public SkillAppService(SkillDomainService skillDomainService) {
        this.skillDomainService = skillDomainService;
    }

    public SkillDTO createSkill(CreateSkillRequest request, String userId) {
        SkillEntity entity = SkillAssembler.fromCreateRequest(request, userId);
        return SkillAssembler.toDTO(skillDomainService.createSkill(entity));
    }

    public SkillDTO updateSkill(String id, UpdateSkillRequest request, String userId) {
        SkillEntity entity = SkillAssembler.fromUpdateRequest(request, id);
        return SkillAssembler.toDTO(skillDomainService.updateSkill(entity, userId));
    }

    public void deleteSkill(String id, String userId) {
        skillDomainService.deleteSkill(id, userId);
    }

    public IPage<SkillDTO> getUserSkills(String userId, SkillQueryRequest request) {
        SkillQuery query = SkillAssembler.fromQueryRequest(request);
        IPage<SkillEntity> entityPage = skillDomainService.queryUserSkills(userId, query);
        return toDTOPage(entityPage);
    }

    private IPage<SkillDTO> toDTOPage(IPage<SkillEntity> entityPage) {
        Page<SkillDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(SkillAssembler::toDTO).toList());
        return dtoPage;
    }
}
