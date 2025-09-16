package org.xhy.community.application.course.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.user.service.UserDomainService;

@Component
public class CourseCDKEventListener {
    
    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    
    public CourseCDKEventListener(CourseDomainService courseDomainService,
                                UserDomainService userDomainService) {
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
    }
    
    @EventListener
    public void handleCDKActivated(CDKActivatedEvent event) {
        if (event.getCdkType() == CDKType.COURSE) {
            // 处理课程CDK激活逻辑
            
            // 1. 验证课程存在
            courseDomainService.getCourseById(event.getTargetId());
            
            // 2. 授予用户课程权限
            userDomainService.grantCourseToUser(event.getUserId(), event.getTargetId());
        }
    }
}