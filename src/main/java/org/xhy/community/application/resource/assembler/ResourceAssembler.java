package org.xhy.community.application.resource.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.dto.ResourceDTO;
import org.xhy.community.application.resource.dto.UploadCredentialsDTO;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.infrastructure.config.AliyunOssProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
    
    public static UploadCredentialsDTO toUploadCredentialsDTO(Map<String, Object> credentials, 
                                                            AliyunOssProperties ossProperties) {
        UploadCredentialsDTO dto = new UploadCredentialsDTO();
        
        // STS临时凭证
        dto.setAccessKeyId((String) credentials.get("accessKeyId"));
        dto.setAccessKeySecret((String) credentials.get("accessKeySecret"));
        dto.setSecurityToken((String) credentials.get("securityToken"));
        dto.setExpiration((String) credentials.get("expiration"));
        
        // OSS信息
        dto.setRegion((String) credentials.get("region"));
        dto.setBucket((String) credentials.get("bucket"));
        dto.setEndpoint((String) credentials.get("endpoint"));
        
        // 上传策略和签名
        dto.setPolicy((String) credentials.get("policy"));
        dto.setSignature((String) credentials.get("signature"));
        dto.setKey((String) credentials.get("key"));
        
        // 回调参数
        dto.setCallback((String) credentials.get("callback"));
        
        // 设置兼容性字段
        dto.setFileKey((String) credentials.get("key"));
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