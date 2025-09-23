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
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.infrastructure.config.AliyunOssProperties;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.resource.request.OssCallbackRequest;
import org.xhy.community.interfaces.resource.request.ResourceQueryRequest;
import org.xhy.community.domain.resource.query.ResourceQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceAppService {
    
    private final ResourceDomainService resourceDomainService;
    private final AliyunOssProperties ossProperties;
    
    public ResourceAppService(ResourceDomainService resourceDomainService, AliyunOssProperties ossProperties) {
        this.resourceDomainService = resourceDomainService;
        this.ossProperties = ossProperties;
    }
    
    public UploadCredentialsDTO getUploadCredentials(String originalName, String contentType) {
        String userId = UserContext.getCurrentUserId();
        String fileKey = generateFileKey(userId, originalName);
        
        Map<String, Object> credentials = resourceDomainService.getStsCredentials(fileKey);
        
        return ResourceAssembler.toUploadCredentialsDTO(credentials, ossProperties);
    }
    

    public ResourceDTO handleOssCallback(OssCallbackRequest callbackRequest) {
        // 从filename中提取关键信息
        String fileKey = callbackRequest.getFilename();
        String originalName = extractOriginalName(fileKey);
        
        // 创建资源实体
        ResourceEntity resource = resourceDomainService.saveResourceFromCallback(
            fileKey, 
            originalName,
            callbackRequest.getMimeType(),
            callbackRequest.getSize()
        );
        
        return ResourceAssembler.toDTO(resource);
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
        
        ResourceQuery query = new ResourceQuery(request.getPageNum(), request.getPageSize());
        query.setUserId(userId);
        query.setResourceType(resourceType);
        
        IPage<ResourceEntity> page = resourceDomainService.getResources(query, AccessLevel.USER);
        
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
    
    private String extractOriginalName(String fileKey) {
        // 从fileKey中提取原始文件名的逻辑
        // 这里简化处理，实际可能需要更复杂的逻辑
        String[] parts = fileKey.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : fileKey;
    }
}
