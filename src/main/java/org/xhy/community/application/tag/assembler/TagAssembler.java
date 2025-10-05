package org.xhy.community.application.tag.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.tag.dto.TagDefinitionDTO;
import org.xhy.community.application.tag.dto.UserTagDTO;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;
import org.xhy.community.domain.tag.entity.UserTagAssignmentEntity;
import org.xhy.community.domain.tag.entity.TagScopeEntity;
import org.xhy.community.interfaces.tag.request.AddScopeRequest;
import org.xhy.community.interfaces.tag.request.CreateTagRequest;
import org.xhy.community.interfaces.tag.request.TagQueryRequest;
import org.xhy.community.interfaces.tag.request.UpdateTagRequest;
import org.xhy.community.domain.tag.valueobject.TagTargetType;
import org.xhy.community.domain.tag.query.TagQuery;

public class TagAssembler {

    public static TagDefinitionDTO toDTO(TagDefinitionEntity e) {
        if (e == null) return null;
        TagDefinitionDTO dto = new TagDefinitionDTO();
        BeanUtils.copyProperties(e, dto);
        return dto;
    }

    public static UserTagDTO toUserTagDTO(UserTagAssignmentEntity a, TagDefinitionEntity d) {
        if (a == null || d == null) return null;
        UserTagDTO dto = new UserTagDTO();
        dto.setTagId(d.getId());
        dto.setCode(d.getCode());
        dto.setName(d.getName());
        dto.setCategory(d.getCategory());
        dto.setIconUrl(d.getIconUrl());
        dto.setDescription(d.getDescription());
        dto.setIssuedAt(a.getIssuedAt());
        dto.setSourceType(a.getSourceType());
        dto.setSourceId(a.getSourceId());
        dto.setMeta(a.getMeta());
        return dto;
    }

    // ====== from Request to Entity ======

    public static TagDefinitionEntity fromCreateRequest(CreateTagRequest req) {
        if (req == null) return null;
        TagDefinitionEntity e = new TagDefinitionEntity();
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setCategory(req.getCategory());
        e.setIconUrl(req.getIconUrl());
        e.setDescription(req.getDescription());
        e.setPublicVisible(Boolean.TRUE.equals(req.getPublicVisible()));
        e.setUniquePerUser(Boolean.TRUE.equals(req.getUniquePerUser()));
        e.setEnabled(Boolean.TRUE.equals(req.getEnabled()));
        return e;
    }

    public static TagDefinitionEntity fromUpdateRequest(UpdateTagRequest req, String tagId) {
        if (req == null) return null;
        TagDefinitionEntity e = new TagDefinitionEntity();
        e.setId(tagId);
        e.setName(req.getName());
        e.setCategory(req.getCategory());
        e.setIconUrl(req.getIconUrl());
        e.setDescription(req.getDescription());
        e.setPublicVisible(req.getPublicVisible());
        e.setUniquePerUser(req.getUniquePerUser());
        e.setEnabled(req.getEnabled());
        return e;
    }

    public static TagScopeEntity fromAddScopeRequest(String tagId, AddScopeRequest req) {
        if (req == null) return null;
        TagScopeEntity s = new TagScopeEntity();
        s.setTagId(tagId);
        // 仅支持课程范围绑定，强制设置为 COURSE
        s.setTargetType(TagTargetType.COURSE);
        s.setTargetId(req.getTargetId());
        return s;
    }

    public static TagQuery fromTagQueryRequest(TagQueryRequest req) {
        TagQuery q = new TagQuery();
        if (req != null) {
            q.setName(req.getName());
            q.setCategory(req.getCategory());
            q.setEnabled(req.getEnabled());
            // 分页
            if (req.getPageNum() != null) q.setPageNum(req.getPageNum());
            if (req.getPageSize() != null) q.setPageSize(req.getPageSize());
        }
        return q;
    }
}
