package org.xhy.community.application.cdk.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.cdk.assembler.CDKAssembler;
import org.xhy.community.application.cdk.dto.CDKDTO;
import org.xhy.community.domain.cdk.entity.CDKEntity;
import org.xhy.community.domain.cdk.service.CDKDomainService;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.cdk.request.CreateCDKRequest;
import org.xhy.community.interfaces.cdk.request.CDKQueryRequest;
import org.xhy.community.domain.cdk.query.CDKQuery;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminCDKAppService {
    
    private final CDKDomainService cdkDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    
    public AdminCDKAppService(CDKDomainService cdkDomainService,
                            SubscriptionPlanDomainService subscriptionPlanDomainService,
                            CourseDomainService courseDomainService,
                            UserDomainService userDomainService) {
        this.cdkDomainService = cdkDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
    }
    
    public List<CDKDTO> createCDK(CreateCDKRequest request) {
        validateTarget(request.getCdkType(), request.getTargetId());

        List<CDKEntity> cdkList = cdkDomainService.createCDKBatch(
            request.getCdkType(),
            request.getTargetId(),
            request.getQuantity(),
            request.getAcquisitionType(),
            request.getRemark(),
            request.getPrice(),
            request.getSubscriptionStrategy()
        );

        String targetName = getTargetName(request.getCdkType(), request.getTargetId());

        return cdkList.stream()
                     .map(entity -> CDKAssembler.toDTOWithTargetName(entity, targetName))
                     .collect(Collectors.toList());
    }
    
    public void deleteCDK(String id) {
        cdkDomainService.deleteCDK(id);
    }
    
    public IPage<CDKDTO> getPagedCDKs(CDKQueryRequest request) {
        CDKQuery query = new CDKQuery(request.getPageNum(), request.getPageSize());
        query.setCdkType(request.getCdkType());
        query.setTargetId(request.getTargetId());
        query.setStatus(request.getStatus());
        query.setAcquisitionType(request.getAcquisitionType()); // 新增
        query.setCode(request.getCode()); // 新增

        IPage<CDKEntity> entityPage = cdkDomainService.getPagedCDKs(query);

        // 批量查询使用者昵称映射
        Set<String> userIds = entityPage.getRecords().stream()
                .map(CDKEntity::getUsedByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, UserEntity> userEntityMap = userDomainService.getUserEntityMapByIds(userIds);
        Map<String, String> userNameMap = userEntityMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));

        List<CDKDTO> dtos = entityPage.getRecords().stream().map(entity -> {
            String targetName = getTargetName(entity.getCdkType(), entity.getTargetId());
            CDKDTO dto = CDKAssembler.toDTOWithTargetName(entity, targetName);
            if (dto != null) {
                dto.setUsedByUserName(userNameMap.get(entity.getUsedByUserId()));
            }
            return dto;
        }).collect(Collectors.toList());

        Page<CDKDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }
    
    private void validateTarget(CDKType cdkType, String targetId) {
        if (cdkType == CDKType.SUBSCRIPTION_PLAN) {
            subscriptionPlanDomainService.getSubscriptionPlanById(targetId);
        } else if (cdkType == CDKType.COURSE) {
            courseDomainService.getCourseById(targetId);
        }
    }
    
    private String getTargetName(CDKType cdkType, String targetId) {
        try {
            if (cdkType == CDKType.SUBSCRIPTION_PLAN) {
                return subscriptionPlanDomainService.getSubscriptionPlanById(targetId).getName();
            } else if (cdkType == CDKType.COURSE) {
                return courseDomainService.getCourseById(targetId).getTitle();
            }
        } catch (Exception e) {
            return "未知";
        }
        return "未知";
    }
}
