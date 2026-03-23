package org.xhy.community.application.skill.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.skill.dto.SkillDTO;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;
import org.xhy.community.interfaces.skill.request.UpdateSkillRequest;

public class SkillAssembler {

    public static SkillDTO toDTO(SkillEntity entity) {
        if (entity == null) {
            return null;
        }
        SkillDTO dto = new SkillDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static SkillEntity fromCreateRequest(CreateSkillRequest request, String userId) {
        SkillEntity entity = new SkillEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setUserId(userId);
        normalize(entity);
        return entity;
    }

    public static SkillEntity fromUpdateRequest(UpdateSkillRequest request, String id) {
        SkillEntity entity = new SkillEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        normalize(entity);
        return entity;
    }

    public static SkillQuery fromQueryRequest(SkillQueryRequest request) {
        return new SkillQuery(request.getPageNum(), request.getPageSize());
    }

    private static void normalize(SkillEntity entity) {
        entity.setName(entity.getName().trim());
        entity.setSummary(entity.getSummary().trim());
        entity.setDescription(entity.getDescription().trim());
        entity.setGithubUrl(entity.getGithubUrl().trim());
    }
}
