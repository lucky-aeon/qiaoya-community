package org.xhy.community.domain.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.domain.resource.repository.ResourceRepository;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ResourceErrorCode;
import org.xhy.community.infrastructure.service.S3Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ResourceDomainService {
    
    private final ResourceRepository resourceRepository;
    private final S3Service s3Service;
    
    public ResourceDomainService(ResourceRepository resourceRepository, S3Service s3Service) {
        this.resourceRepository = resourceRepository;
        this.s3Service = s3Service;
    }
    
    public ResourceEntity uploadFile(String userId, String originalName, String contentType, 
                                   long size, InputStream inputStream) {
        // 生成文件key
        String fileKey = generateFileKey(userId, originalName);
        
        try {
            // 上传文件到S3
            s3Service.uploadFile(fileKey, inputStream, contentType, size);
            
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
    
    public String generatePresignedUploadUrl(String userId, String originalName, String contentType) {
        String fileKey = generateFileKey(userId, originalName);
        return s3Service.generatePresignedUploadUrl(fileKey, contentType);
    }
    
    public String generatePresignedUploadUrl(String fileKey, String contentType) {
        return s3Service.generatePresignedUploadUrl(fileKey, contentType);
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
        return s3Service.generatePresignedDownloadUrl(resource.getFileKey());
    }
    
    public List<ResourceEntity> getUserResources(String userId) {
        LambdaQueryWrapper<ResourceEntity> queryWrapper = new LambdaQueryWrapper<ResourceEntity>()
                .eq(ResourceEntity::getUserId, userId)
                .orderByDesc(ResourceEntity::getCreateTime);
        
        return resourceRepository.selectList(queryWrapper);
    }
    
    public IPage<ResourceEntity> getUserResources(String userId, int pageNum, int pageSize, 
                                                ResourceType resourceType) {
        Page<ResourceEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ResourceEntity> queryWrapper = new LambdaQueryWrapper<ResourceEntity>()
                .eq(ResourceEntity::getUserId, userId)
                .eq(resourceType != null, ResourceEntity::getResourceType, resourceType)
                .orderByDesc(ResourceEntity::getCreateTime);
        
        return resourceRepository.selectPage(page, queryWrapper);
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