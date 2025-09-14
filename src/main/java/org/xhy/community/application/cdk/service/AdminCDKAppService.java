package org.xhy.community.application.cdk.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.cdk.assembler.CDKAssembler;
import org.xhy.community.application.cdk.dto.CDKDTO;
import org.xhy.community.domain.cdk.entity.CDKEntity;
import org.xhy.community.domain.cdk.service.CDKDomainService;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.interfaces.cdk.request.CreateCDKRequest;
import org.xhy.community.interfaces.cdk.request.CDKQueryRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCDKAppService {
    
    private final CDKDomainService cdkDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CourseDomainService courseDomainService;
    
    public AdminCDKAppService(CDKDomainService cdkDomainService,
                            SubscriptionPlanDomainService subscriptionPlanDomainService,
                            CourseDomainService courseDomainService) {
        this.cdkDomainService = cdkDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.courseDomainService = courseDomainService;
    }
    
    public List<CDKDTO> createCDK(CreateCDKRequest request) {
        validateTarget(request.getCdkType(), request.getTargetId());
        
        List<CDKEntity> cdkList = cdkDomainService.createCDKBatch(
            request.getCdkType(), 
            request.getTargetId(), 
            request.getQuantity()
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
        IPage<CDKEntity> entityPage = cdkDomainService.getPagedCDKs(
            request.getPageNum(), 
            request.getPageSize(), 
            request.getCdkType(), 
            request.getTargetId(), 
            request.getStatus()
        );
        
        return entityPage.convert(entity -> {
            String targetName = getTargetName(entity.getCdkType(), entity.getTargetId());
            return CDKAssembler.toDTOWithTargetName(entity, targetName);
        });
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