package org.xhy.community.application.tag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.tag.assembler.TagAssembler;
import org.xhy.community.application.tag.dto.TagDefinitionDTO;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;

@Service
public class AdminTagAppService {

    private final org.xhy.community.domain.tag.service.TagDomainService tagDomainService;

    public AdminTagAppService(org.xhy.community.domain.tag.service.TagDomainService tagDomainService) {
        this.tagDomainService = tagDomainService;
    }

    public TagDefinitionDTO createTag(org.xhy.community.interfaces.tag.request.CreateTagRequest req) {
        TagDefinitionEntity e = TagAssembler.fromCreateRequest(req);
        TagDefinitionEntity created = tagDomainService.createTag(e);
        return TagAssembler.toDTO(created);
    }

    public TagDefinitionDTO updateTag(String id, org.xhy.community.interfaces.tag.request.UpdateTagRequest req) {
        TagDefinitionEntity toUpdate = TagAssembler.fromUpdateRequest(req, id);
        TagDefinitionEntity updated = tagDomainService.updateTag(toUpdate);
        return TagAssembler.toDTO(updated);
    }

    public IPage<TagDefinitionDTO> listTags(org.xhy.community.interfaces.tag.request.TagQueryRequest q) {
        var query = TagAssembler.fromTagQueryRequest(q);
        return tagDomainService
                .listTags(query)
                .convert(TagAssembler::toDTO);
    }

    public void addScope(String tagId, org.xhy.community.interfaces.tag.request.AddScopeRequest req) {
        org.xhy.community.domain.tag.entity.TagScopeEntity scope =
                org.xhy.community.application.tag.assembler.TagAssembler.fromAddScopeRequest(tagId, req);
        tagDomainService.addScope(scope);
    }

    public void removeScope(String scopeId) {
        tagDomainService.removeScope(scopeId);
    }

    public java.util.List<org.xhy.community.application.tag.dto.TagScopeDTO>  listScopes(String tagId) {
        return tagDomainService.listScopesByTagId(tagId)
                .stream().map(org.xhy.community.application.tag.dto.TagScopeDTO::fromEntity).toList();
    }

    public void assignTagToUser(org.xhy.community.interfaces.tag.request.ManualAssignRequest req) {
        tagDomainService.assignTagToUser(req.getUserId(), req.getTagId(), req.getSourceType(), req.getSourceId());
    }

    public void revokeUserTag(org.xhy.community.interfaces.tag.request.ManualRevokeRequest req) {
        tagDomainService.revokeTagFromUser(req.getUserId(), req.getTagId());
    }
}
