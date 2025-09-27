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
import org.xhy.community.domain.resourcebinding.service.ResourceBindingDomainService;
import org.xhy.community.domain.resourcebinding.entity.ResourceBindingEntity;
import org.xhy.community.domain.resourcebinding.valueobject.ResourceTargetType;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.application.permission.service.UserPermissionAppService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

import static org.xhy.community.infrastructure.exception.ResourceErrorCode.ACCESS_DENIED;

@Service
public class ResourceAppService {
    
    private final ResourceDomainService resourceDomainService;
    private final AliyunOssProperties ossProperties;
    private final ResourceBindingDomainService resourceBindingDomainService;
    private final ChapterDomainService chapterDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final UserDomainService userDomainService;
    private final UserPermissionAppService userPermissionAppService;
    
    public ResourceAppService(ResourceDomainService resourceDomainService,
                              AliyunOssProperties ossProperties,
                              ResourceBindingDomainService resourceBindingDomainService,
                              ChapterDomainService chapterDomainService,
                              SubscriptionDomainService subscriptionDomainService,
                              SubscriptionPlanDomainService subscriptionPlanDomainService,
                              UserDomainService userDomainService,
                              UserPermissionAppService userPermissionAppService) {
        this.resourceDomainService = resourceDomainService;
        this.ossProperties = ossProperties;
        this.resourceBindingDomainService = resourceBindingDomainService;
        this.chapterDomainService = chapterDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.userDomainService = userDomainService;
        this.userPermissionAppService = userPermissionAppService;
    }
    
    public UploadCredentialsDTO getUploadCredentials(String originalName, String contentType, String token) {
        String userId = UserContext.getCurrentUserId();
        String fileKey = generateFileKey(userId, originalName);
        
        Map<String, Object> credentials = resourceDomainService.getStsCredentials(fileKey, token);
        
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

    /**
     * 生成资源访问URL（受课程权限控制）
     * App 层不依赖 UserContext，由 API 层传入 userId
     */
    public String getResourceAccessUrl(String resourceId, String userId) {
        // 读取绑定关系
        java.util.List<ResourceBindingEntity> bindings = resourceBindingDomainService.getBindingsByResourceId(resourceId);
        if (bindings == null || bindings.isEmpty()) {
            // 未绑定，直接放行
            return resourceDomainService.getDownloadUrl(resourceId);
        }

        java.util.Set<String> courseIds = new java.util.HashSet<>();
        java.util.Set<String> chapterIds = bindings.stream()
                .filter(b -> b.getTargetType() == ResourceTargetType.CHAPTER)
                .map(ResourceBindingEntity::getTargetId)
                .collect(java.util.stream.Collectors.toSet());
        if (!chapterIds.isEmpty()) {
            java.util.Map<String, String> chapterCourseMap = chapterDomainService.getChapterCourseIdMapByIds(chapterIds);
            courseIds.addAll(chapterCourseMap.values());
        }
        bindings.stream()
                .filter(b -> b.getTargetType() == ResourceTargetType.COURSE)
                .map(ResourceBindingEntity::getTargetId)
                .forEach(courseIds::add);

        if (courseIds.isEmpty()) {
            // 绑定到了未知对象，默认放行
            return resourceDomainService.getDownloadUrl(resourceId);
        }

        // 判定用户是否解锁任一课程（复用统一权限应用服务）
        if (userPermissionAppService.hasAccessToAnyCourse(userId, courseIds)) {
            return resourceDomainService.getDownloadUrl(resourceId);
        }

        // 未解锁：拒绝访问
        throw new org.xhy.community.infrastructure.exception.BusinessException(
               ACCESS_DENIED);
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
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalName);
        return String.format("%s/%s.%s", userId, uuid, extension);
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
