package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.UserCourseOwnershipAssembler;
import org.xhy.community.application.course.dto.UserCourseOwnershipDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.user.entity.UserCourseEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserCourseOwnershipAppService {

    private final UserDomainService userDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CourseDomainService courseDomainService;

    public UserCourseOwnershipAppService(UserDomainService userDomainService,
                                         SubscriptionDomainService subscriptionDomainService,
                                         SubscriptionPlanDomainService subscriptionPlanDomainService,
                                         CourseDomainService courseDomainService) {
        this.userDomainService = userDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.courseDomainService = courseDomainService;
    }

    /**
     * 查询当前用户当前有效的课程权益
     */
    public List<UserCourseOwnershipDTO> getCurrentUserCourseOwnerships(String userId) {
        List<UserCourseEntity> directCourses = userDomainService.getUserCourseEntities(userId);
        List<UserSubscriptionEntity> activeSubscriptions = subscriptionDomainService.getUserActiveSubscriptions(userId);

        Set<String> planIds = activeSubscriptions.stream()
                .map(UserSubscriptionEntity::getSubscriptionPlanId)
                .collect(Collectors.toSet());
        Map<String, SubscriptionPlanEntity> planMap = subscriptionPlanDomainService.getSubscriptionPlansByIds(planIds).stream()
                .collect(Collectors.toMap(SubscriptionPlanEntity::getId, plan -> plan));

        Map<String, List<UserCourseOwnershipDTO.OwnershipSourceDTO>> sourcesByCourseId = new LinkedHashMap<>();

        for (UserCourseEntity userCourse : directCourses) {
            addSource(sourcesByCourseId, userCourse.getCourseId(),
                    UserCourseOwnershipAssembler.directCourseSource(
                            userCourse.getId(),
                            userCourse.getCourseId(),
                            userCourse.getCreateTime()
                    ));
        }

        for (UserSubscriptionEntity subscription : activeSubscriptions) {
            SubscriptionPlanEntity plan = planMap.get(subscription.getSubscriptionPlanId());
            if (plan == null) {
                continue;
            }

            List<String> courseIds = subscriptionPlanDomainService.getSubscriptionPlanCourseIds(plan.getId());
            for (String courseId : courseIds) {
                addSource(sourcesByCourseId, courseId,
                        UserCourseOwnershipAssembler.subscriptionPlanSource(
                                subscription.getId(),
                                plan.getId(),
                                plan.getName(),
                                subscription.getStartTime(),
                                subscription.getEndTime()
                        ));
            }
        }

        if (sourcesByCourseId.isEmpty()) {
            return List.of();
        }

        Map<String, CourseEntity> courseMap = courseDomainService.getCourseEntityMapByIds(sourcesByCourseId.keySet());

        return sourcesByCourseId.entrySet().stream()
                .map(entry -> UserCourseOwnershipAssembler.toDTO(courseMap.get(entry.getKey()), entry.getValue()))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator
                        .comparing(UserCourseOwnershipDTO::isPermanent)
                        .reversed()
                        .thenComparing(UserCourseOwnershipDTO::getEffectiveTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private void addSource(Map<String, List<UserCourseOwnershipDTO.OwnershipSourceDTO>> sourcesByCourseId,
                           String courseId,
                           UserCourseOwnershipDTO.OwnershipSourceDTO source) {
        sourcesByCourseId.computeIfAbsent(courseId, key -> new ArrayList<>()).add(source);
    }
}
