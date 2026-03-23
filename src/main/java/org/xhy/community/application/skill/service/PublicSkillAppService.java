package org.xhy.community.application.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.skill.assembler.SkillAssembler;
import org.xhy.community.application.skill.dto.SkillDTO;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.skill.service.SkillDomainService;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;

@Service
public class PublicSkillAppService {

    private final SkillDomainService skillDomainService;

    public PublicSkillAppService(SkillDomainService skillDomainService) {
        this.skillDomainService = skillDomainService;
    }

    public IPage<SkillDTO> getPublicSkills(SkillQueryRequest request) {
        SkillQuery query = SkillAssembler.fromQueryRequest(request);
        IPage<SkillEntity> entityPage = skillDomainService.queryPublicSkills(query);
        Page<SkillDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(SkillAssembler::toDTO).toList());
        return dtoPage;
    }
}
