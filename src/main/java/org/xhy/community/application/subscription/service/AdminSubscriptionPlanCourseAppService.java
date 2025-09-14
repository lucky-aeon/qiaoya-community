package org.xhy.community.application.subscription.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.subscription.dto.SimpleSubscriptionPlanDTO;
import org.xhy.community.application.course.dto.SimpleCourseDTO;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanCoursesRequest;

import java.util.List;

@Service
public class AdminSubscriptionPlanCourseAppService {
    
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CourseDomainService courseDomainService;
    
    public AdminSubscriptionPlanCourseAppService(SubscriptionPlanDomainService subscriptionPlanDomainService,
                                               CourseDomainService courseDomainService) {
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.courseDomainService = courseDomainService;
    }
    
    public List<SimpleSubscriptionPlanDTO> getSimpleSubscriptionPlans() {
        return subscriptionPlanDomainService.getAllSimpleSubscriptionPlans();
    }
    
    public List<SimpleCourseDTO> getSimpleCourses() {
        return courseDomainService.getAllSimpleCourses();
    }
    
    public List<String> getSubscriptionPlanCourseIds(String planId) {
        return subscriptionPlanDomainService.getSubscriptionPlanCourseIds(planId);
    }
    
    public void updateSubscriptionPlanCourses(String planId, UpdateSubscriptionPlanCoursesRequest request) {
        subscriptionPlanDomainService.syncSubscriptionPlanCourses(planId, request.getCourseIds());
    }
}