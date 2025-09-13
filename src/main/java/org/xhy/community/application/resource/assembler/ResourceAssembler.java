package org.xhy.community.application.resource.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.dto.ResourceDTO;
import org.xhy.community.application.resource.dto.UploadCredentialsDTO;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.infrastructure.config.AwsProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceAssembler {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static ResourceDTO toDTO(ResourceEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ResourceDTO dto = new ResourceDTO();
        BeanUtils.copyProperties(entity, dto);
        
        if (entity.getResourceType() != null) {
            dto.setResourceType(entity.getResourceType().name());
        }
        
        if (entity.getCreateTime() != null) {
            dto.setCreateTime(entity.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        
        if (entity.getUpdateTime() != null) {
            dto.setUpdateTime(entity.getUpdateTime().format(DATE_TIME_FORMATTER));
        }
        
        return dto;
    }
    
    public static UploadCredentialsDTO toUploadCredentialsDTO(String uploadUrl, String fileKey, 
                                                            AwsProperties awsProperties) {
        UploadCredentialsDTO dto = new UploadCredentialsDTO();
        dto.setUploadUrl(uploadUrl);
        dto.setFileKey(fileKey);
        dto.setBucket(awsProperties.getS3().getBucket());
        dto.setExpiration(LocalDateTime.now().plusSeconds(awsProperties.getS3().getPresignedUrlExpiration()));
        dto.setMaxFileSize(100 * 1024 * 1024L); // 默认100MB限制
        
        return dto;
    }
    
    public static PagedResourceDTO toPagedResourceDTO(IPage<ResourceEntity> page) {
        PagedResourceDTO dto = new PagedResourceDTO();
        
        List<ResourceDTO> records = page.getRecords().stream()
                .map(ResourceAssembler::toDTO)
                .collect(Collectors.toList());
        
        dto.setRecords(records);
        dto.setTotal(page.getTotal());
        dto.setPageNum((int) page.getCurrent());
        dto.setPageSize((int) page.getSize());
        dto.setPages((int) page.getPages());
        
        return dto;
    }
}