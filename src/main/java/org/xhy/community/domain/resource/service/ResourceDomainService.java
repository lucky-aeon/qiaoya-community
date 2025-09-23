package org.xhy.community.domain.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.domain.resource.repository.ResourceRepository;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ResourceErrorCode;
import org.xhy.community.infrastructure.service.AliyunOssService;
import org.xhy.community.domain.resource.query.ResourceQuery;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ResourceDomainService {
    
    private final ResourceRepository resourceRepository;
    private final AliyunOssService aliyunOssService;
    
    public ResourceDomainService(ResourceRepository resourceRepository, AliyunOssService aliyunOssService) {
        this.resourceRepository = resourceRepository;
        this.aliyunOssService = aliyunOssService;
    }
    
    public ResourceEntity uploadFile(String userId, String originalName, String contentType, 
                                   long size, InputStream inputStream) {
        // 生成文件key
        String fileKey = generateFileKey(userId, originalName);
        
        try {
            // 上传文件到OSS
            aliyunOssService.uploadFile(fileKey, inputStream, contentType);
            
            // 创建资源实体
            ResourceEntity resource = new ResourceEntity();
            resource.setFileKey(fileKey);
            resource.setSize(size);
            resource.setFormat(getFileExtension(originalName));
            resource.setUserId(userId);
            resource.setOriginalName(originalName);
            resource.initializeResourceType();
            
            // 保存到数据库
            resourceRepository.insert(resource);
            
            return resource;
        } catch (Exception e) {
            throw new BusinessException(ResourceErrorCode.UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getStsCredentials(String fileKey) {
        return aliyunOssService.getStsCredentials(fileKey);
    }
    
    public String generatePresignedUploadUrl(String userId, String originalName, String contentType) {
        String fileKey = generateFileKey(userId, originalName);
        return generatePresignedUploadUrl(fileKey, contentType);
    }
    
    public String generatePresignedUploadUrl(String fileKey, String contentType) {
        // OSS直传不需要预签名URL，这个方法保持兼容性但不使用
        return null;
    }
    

    public ResourceEntity getResourceById(String resourceId) {
        ResourceEntity resource = resourceRepository.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "资源不存在");
        }
        return resource;
    }
    
    public String getDownloadUrl(String resourceId) {
        ResourceEntity resource = getResourceById(resourceId);
        return aliyunOssService.generatePresignedDownloadUrl(resource.getFileKey());
    }
    
    public List<ResourceEntity> getUserResources(String userId) {
        LambdaQueryWrapper<ResourceEntity> queryWrapper = new LambdaQueryWrapper<ResourceEntity>()
                .eq(ResourceEntity::getUserId, userId)
                .orderByDesc(ResourceEntity::getCreateTime);
        
        return resourceRepository.selectList(queryWrapper);
    }
    
    public IPage<ResourceEntity> getUserResources(ResourceQuery query) {
        return getResources(query, AccessLevel.USER);
    }

    public IPage<ResourceEntity> getResources(ResourceQuery query, AccessLevel accessLevel) {
        Page<ResourceEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<ResourceEntity> queryWrapper = new LambdaQueryWrapper<ResourceEntity>()
                // 仅在用户级访问时添加用户隔离条件
                .eq(accessLevel == AccessLevel.USER && StringUtils.hasText(query.getUserId()),
                        ResourceEntity::getUserId, query.getUserId())
                .eq(query.getResourceType() != null, ResourceEntity::getResourceType, query.getResourceType())
                .orderByDesc(ResourceEntity::getCreateTime);

        return resourceRepository.selectPage(page, queryWrapper);
    }
    
    public boolean verifyOssCallback(String authorization, String callbackBody, String publicKeyUrl) {
        return aliyunOssService.verifyOssCallback(authorization, callbackBody, publicKeyUrl);
    }
    
    public ResourceEntity saveResourceFromCallback(String fileKey, String originalName, String mimeType, Long size) {
        // 从fileKey中提取userId
        String userId = extractUserIdFromFileKey(fileKey);
        
        // 创建资源实体
        ResourceEntity resource = new ResourceEntity();
        resource.setFileKey(fileKey);
        resource.setSize(size);
        resource.setFormat(getFileExtension(originalName));
        resource.setUserId(userId);
        resource.setOriginalName(originalName);
        resource.initializeResourceType();
        
        // 保存到数据库
        resourceRepository.insert(resource);
        
        return resource;
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
    
    private String extractUserIdFromFileKey(String fileKey) {
        // fileKey格式: uploads/{userId}/{date}/{uuid}.{ext}
        String[] parts = fileKey.split("/");
        if (parts.length > 1) {
            return parts[1]; // userId在第二个位置
        }
        throw new IllegalArgumentException("无法从fileKey中提取用户ID: " + fileKey);
    }
}
