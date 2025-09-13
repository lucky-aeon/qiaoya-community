package org.xhy.community.application.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.application.resource.assembler.ResourceAssembler;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.dto.ResourceDTO;
import org.xhy.community.application.resource.dto.UploadCredentialsDTO;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.domain.resource.service.ResourceDomainService;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.infrastructure.config.AwsProperties;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.resource.request.ResourceQueryRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResourceAppService {
    
    private final ResourceDomainService resourceDomainService;
    private final AwsProperties awsProperties;
    
    public ResourceAppService(ResourceDomainService resourceDomainService, AwsProperties awsProperties) {
        this.resourceDomainService = resourceDomainService;
        this.awsProperties = awsProperties;
    }
    
    public UploadCredentialsDTO getUploadCredentials(String originalName, String contentType) {
        String userId = UserContext.getCurrentUserId();
        String fileKey = generateFileKey(userId, originalName);
        
        String uploadUrl = resourceDomainService.generatePresignedUploadUrl(fileKey, contentType);
        
        return ResourceAssembler.toUploadCredentialsDTO(uploadUrl, fileKey, awsProperties);
    }
    

    public String getResourceAccessUrl(String resourceId) {
        return resourceDomainService.getDownloadUrl(resourceId);
    }
    

    public PagedResourceDTO getUserResources(ResourceQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        
        // 在Application层处理resourceType转换
        ResourceType resourceType = null;
        if (StringUtils.hasText(request.getResourceType())) {
            try {
                resourceType = ResourceType.valueOf(request.getResourceType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效的资源类型，resourceType保持null
            }
        }
        
        IPage<ResourceEntity> page = resourceDomainService.getUserResources(
                userId, 
                request.getPageNum(), 
                request.getPageSize(),
                resourceType
        );
        
        return ResourceAssembler.toPagedResourceDTO(page);
    }

    private String generateFileKey(String userId, String originalName) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalName);
        return String.format("uploads/%s/%s/%s.%s", userId, date, uuid, extension);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}