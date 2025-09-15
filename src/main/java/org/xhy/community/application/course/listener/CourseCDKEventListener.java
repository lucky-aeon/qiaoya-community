package org.xhy.community.application.course.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.course.service.CourseDomainService;

@Component
public class CourseCDKEventListener {
    
    private final CourseDomainService courseDomainService;
    
    public CourseCDKEventListener(CourseDomainService courseDomainService) {
        this.courseDomainService = courseDomainService;
    }
    
    @EventListener
    public void handleCDKActivated(CDKActivatedEvent event) {
        if (event.getCdkType() == CDKType.COURSE) {
            // 处理课程CDK激活逻辑
            // 验证课程存在
            courseDomainService.getCourseById(event.getTargetId());
            
            // TODO: 实现课程权限授予逻辑
            // 当前只做课程存在性验证，实际的权限授予留给将来的权限领域实现
            // courseDomainService.grantCourseAccess(event.getUserId(), event.getTargetId());
        }
    }
}