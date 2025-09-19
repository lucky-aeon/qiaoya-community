package org.xhy.community.application.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.user.dto.UserActivityLogDTO;
import org.xhy.community.application.user.assembler.UserActivityLogAssembler;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;
import org.xhy.community.domain.log.query.UserActivityLogQuery;
import org.xhy.community.application.log.assembler.UserActivityLogQueryAssembler;
import org.xhy.community.interfaces.log.request.QueryUserActivityLogRequest;
import org.xhy.community.infrastructure.exception.ValidationException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;

import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 管理员用户活动日志应用服务
 * 提供管理员级别的用户活动日志查询功能
 */
@Service
public class UserActivityLogAppService {
    
    private final UserActivityLogDomainService userActivityLogDomainService;
    private final UserDomainService userDomainService;
    private final PostDomainService postDomainService;
    private final CourseDomainService courseDomainService;
    private final CategoryDomainService categoryDomainService;
    
    public UserActivityLogAppService(UserActivityLogDomainService userActivityLogDomainService,
                                   UserDomainService userDomainService,
                                   PostDomainService postDomainService,
                                   CourseDomainService courseDomainService,
                                   CategoryDomainService categoryDomainService) {
        this.userActivityLogDomainService = userActivityLogDomainService;
        this.userDomainService = userDomainService;
        this.postDomainService = postDomainService;
        this.courseDomainService = courseDomainService;
        this.categoryDomainService = categoryDomainService;
    }
    
    /**
     * 管理员查询用户活动日志
     * 返回分页的用户活动日志列表（包含用户昵称）
     *
     * @param request 查询请求参数
     * @return 分页日志列表
     */
    public IPage<UserActivityLogDTO> getActivityLogs(QueryUserActivityLogRequest request) {
        // 参数校验：activityType和activityCategory不能同时指定
        validateQueryParameters(request);
        
        // 转换请求对象为查询对象
        UserActivityLogQuery query = UserActivityLogQueryAssembler.toQuery(request);
        
        // 查询分页日志数据
        IPage<UserActivityLogEntity> page = userActivityLogDomainService.getActivityLogs(query);
        
        // 批量获取用户名称
        Set<String> userIds = page.getRecords().stream()
                .map(UserActivityLogEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
                
        Map<String, String> userNameMap = userDomainService.getUserNameMapByIds(userIds);
        
        // 批量获取目标对象名称
        Map<String, String> targetNameMap = getTargetNameMap(page.getRecords());
        
        // 转换为DTO并设置用户昵称和目标名称
        return page.convert(entity -> {
            UserActivityLogDTO dto = UserActivityLogAssembler.toDTO(entity);
            if (dto != null) {
                // 设置用户昵称
                if (entity.getUserId() != null) {
                    dto.setNickname(userNameMap.get(entity.getUserId()));
                }
                // 设置目标对象名称
                if (entity.getTargetId() != null) {
                    dto.setTargetName(targetNameMap.get(entity.getTargetId()));
                }
            }
            return dto;
        });
    }
    
    /**
     * 参数校验：确保activityType和activityCategory互斥
     */
    private void validateQueryParameters(QueryUserActivityLogRequest request) {
        if (request.getActivityType() != null && request.getActivityCategory() != null) {
            throw new ValidationException(
                ValidationErrorCode.PARAM_INVALID,
                "活动类型(activityType)和活动分类(activityCategory)不能同时指定，请选择其中一种查询方式"
            );
        }
    }
    
    /**
     * 根据targetType和targetId批量获取目标对象名称
     */
    private Map<String, String> getTargetNameMap(java.util.List<UserActivityLogEntity> logs) {
        Map<String, String> targetNameMap = new HashMap<>();
        
        // 按targetType分组收集targetId
        Map<String, Set<String>> typeIdMap = logs.stream()
                .filter(log -> log.getTargetType() != null && log.getTargetId() != null)
                .collect(Collectors.groupingBy(
                    UserActivityLogEntity::getTargetType,
                    Collectors.mapping(
                        UserActivityLogEntity::getTargetId,
                        Collectors.toSet()
                    )
                ));
        
        // 批量查询各类型的名称
        for (Map.Entry<String, Set<String>> entry : typeIdMap.entrySet()) {
            String targetType = entry.getKey();
            Set<String> targetIds = entry.getValue();
            
            Map<String, String> names = getNamesByType(targetType, targetIds);
            targetNameMap.putAll(names);
        }
        
        return targetNameMap;
    }
    
    /**
     * 根据类型批量获取名称
     */
    private Map<String, String> getNamesByType(String targetType, Set<String> targetIds) {
        return switch (targetType.toUpperCase()) {
            case "POST" -> postDomainService.getPostTitleMapByIds(targetIds);
            case "COURSE" -> courseDomainService.getCourseTitleMapByIds(targetIds);
            case "CATEGORY" -> categoryDomainService.getCategoryNameMapByIds(targetIds);
            case "USER" -> userDomainService.getUserNameMapByIds(targetIds);
            default -> Map.of(); // 不支持的类型返回空Map
        };
    }
}