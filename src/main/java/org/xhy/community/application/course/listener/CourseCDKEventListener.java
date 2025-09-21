package org.xhy.community.application.course.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.user.service.UserDomainService;

@Component
public class CourseCDKEventListener {

    private static final Logger log = LoggerFactory.getLogger(CourseCDKEventListener.class);

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
            String masked = mask(event.getCdkCode());
            log.info("[CDK激活-监听] 处理课程CDK: userId={}, courseId={}, cdk={}",
                    event.getUserId(), event.getTargetId(), masked);

            // 1. 验证课程存在
            courseDomainService.getCourseById(event.getTargetId());

            // 2. 授予用户课程权限
            userDomainService.grantCourseToUser(event.getUserId(), event.getTargetId());
            log.info("[CDK激活-监听] 已授予课程权限: userId={}, courseId={}", event.getUserId(), event.getTargetId());
        }
    }

    private String mask(String code) {
        if (code == null || code.length() <= 4) return "****";
        int len = code.length();
        return code.substring(0, Math.min(4, len)) + "****" + code.substring(len - 2);
    }
}
