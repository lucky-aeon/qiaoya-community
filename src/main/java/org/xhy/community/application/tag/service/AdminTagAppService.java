package org.xhy.community.application.tag.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.tag.assembler.TagAssembler;
import org.xhy.community.application.tag.dto.TagDefinitionDTO;
import org.xhy.community.application.tag.dto.TagScopeDTO;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;
import org.xhy.community.domain.tag.entity.TagScopeEntity;
import org.xhy.community.domain.tag.service.TagDomainService;
import org.xhy.community.interfaces.tag.request.AddScopeRequest;
import org.xhy.community.interfaces.tag.request.CreateTagRequest;
import org.xhy.community.interfaces.tag.request.ManualAssignRequest;
import org.xhy.community.interfaces.tag.request.ManualRevokeRequest;
import org.xhy.community.interfaces.tag.request.TagQueryRequest;
import org.xhy.community.interfaces.tag.request.UpdateTagRequest;

@Service
public class AdminTagAppService {

    private final TagDomainService tagDomainService;

    public AdminTagAppService(TagDomainService tagDomainService) {
        this.tagDomainService = tagDomainService;
    }

    public TagDefinitionDTO createTag(CreateTagRequest req) {
        TagDefinitionEntity e = TagAssembler.fromCreateRequest(req);
        TagDefinitionEntity created = tagDomainService.createTag(e);
        return TagAssembler.toDTO(created);
    }

    public TagDefinitionDTO updateTag(String id, UpdateTagRequest req) {
        TagDefinitionEntity toUpdate = TagAssembler.fromUpdateRequest(req, id);
        TagDefinitionEntity updated = tagDomainService.updateTag(toUpdate);
        return TagAssembler.toDTO(updated);
    }

    public IPage<TagDefinitionDTO> listTags(TagQueryRequest q) {
        var query = TagAssembler.fromTagQueryRequest(q);
        return tagDomainService
                .listTags(query)
                .convert(TagAssembler::toDTO);
    }

    public void addScope(String tagId, AddScopeRequest req) {
        TagScopeEntity scope =
                TagAssembler.fromAddScopeRequest(tagId, req);
        tagDomainService.addScope(scope);
    }

    public void removeScope(String scopeId) {
        tagDomainService.removeScope(scopeId);
    }

    public java.util.List<TagScopeDTO>  listScopes(String tagId) {
        return tagDomainService.listScopesByTagId(tagId)
                .stream().map(TagScopeDTO::fromEntity).toList();
    }

    public void assignTagToUser(ManualAssignRequest req) {
        tagDomainService.assignTagToUser(req.getUserId(), req.getTagId(), req.getSourceType(), req.getSourceId());
    }

    public void revokeUserTag(ManualRevokeRequest req) {
        tagDomainService.revokeTagFromUser(req.getUserId(), req.getTagId());
    }
}
