package org.xhy.community.application.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.application.resource.assembler.ResourceAssembler;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.domain.resource.service.ResourceDomainService;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.domain.resource.query.ResourceQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.interfaces.resource.request.ResourceQueryRequest;

@Service
public class AdminResourceAppService {

    private final ResourceDomainService resourceDomainService;

    public AdminResourceAppService(ResourceDomainService resourceDomainService) {
        this.resourceDomainService = resourceDomainService;
    }

    public PagedResourceDTO getResources(ResourceQueryRequest request) {
        // Application层处理resourceType转换
        ResourceType resourceType = null;
        if (StringUtils.hasText(request.getResourceType())) {
            try {
                resourceType = ResourceType.valueOf(request.getResourceType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效的资源类型
            }
        }

        ResourceQuery query = new ResourceQuery(request.getPageNum(), request.getPageSize());
        query.setResourceType(resourceType);
        // 管理员不限定用户

        IPage<ResourceEntity> page = resourceDomainService.getResources(query, AccessLevel.ADMIN);
        return ResourceAssembler.toPagedResourceDTO(page);
    }
}

