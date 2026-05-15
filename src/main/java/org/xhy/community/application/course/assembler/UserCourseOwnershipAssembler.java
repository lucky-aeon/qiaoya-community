package org.xhy.community.application.course.assembler;

import org.xhy.community.application.course.dto.UserCourseOwnershipDTO;
import org.xhy.community.domain.course.entity.CourseEntity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class UserCourseOwnershipAssembler {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String SOURCE_DIRECT_COURSE = "DIRECT_COURSE";
    private static final String SOURCE_SUBSCRIPTION_PLAN = "SUBSCRIPTION_PLAN";

    public static UserCourseOwnershipDTO toDTO(CourseEntity course, List<UserCourseOwnershipDTO.OwnershipSourceDTO> sources) {
        if (course == null || sources == null || sources.isEmpty()) {
            return null;
        }

        sources.sort(Comparator
                .comparing(UserCourseOwnershipDTO.OwnershipSourceDTO::isPermanent)
                .reversed()
                .thenComparing(UserCourseOwnershipDTO.OwnershipSourceDTO::getEffectiveTime,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        UserCourseOwnershipDTO dto = new UserCourseOwnershipDTO();
        dto.setCourseId(course.getId());
        dto.setCourseTitle(course.getTitle());
        dto.setCoverImage(course.getCoverImage());
        dto.setCourseStatus(course.getStatus());
        dto.setOwnershipStatus(STATUS_ACTIVE);
        dto.setSources(sources);

        LocalDateTime effectiveTime = sources.stream()
                .map(UserCourseOwnershipDTO.OwnershipSourceDTO::getEffectiveTime)
                .filter(java.util.Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        boolean permanent = sources.stream().anyMatch(UserCourseOwnershipDTO.OwnershipSourceDTO::isPermanent);
        LocalDateTime expireTime = permanent ? null : sources.stream()
                .map(UserCourseOwnershipDTO.OwnershipSourceDTO::getExpireTime)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        dto.setEffectiveTime(effectiveTime);
        dto.setExpireTime(expireTime);
        dto.setPermanent(permanent);
        return dto;
    }

    public static UserCourseOwnershipDTO.OwnershipSourceDTO directCourseSource(String recordId,
                                                                               String courseId,
                                                                               LocalDateTime effectiveTime) {
        UserCourseOwnershipDTO.OwnershipSourceDTO dto = new UserCourseOwnershipDTO.OwnershipSourceDTO();
        dto.setSourceType(SOURCE_DIRECT_COURSE);
        dto.setSourceRecordId(recordId);
        dto.setSourceBusinessId(courseId);
        dto.setSourceName("单课购买");
        dto.setStatus(STATUS_ACTIVE);
        dto.setEffectiveTime(effectiveTime);
        dto.setExpireTime(null);
        dto.setPermanent(true);
        return dto;
    }

    public static UserCourseOwnershipDTO.OwnershipSourceDTO subscriptionPlanSource(String recordId,
                                                                                   String planId,
                                                                                   String planName,
                                                                                   LocalDateTime effectiveTime,
                                                                                   LocalDateTime expireTime) {
        UserCourseOwnershipDTO.OwnershipSourceDTO dto = new UserCourseOwnershipDTO.OwnershipSourceDTO();
        dto.setSourceType(SOURCE_SUBSCRIPTION_PLAN);
        dto.setSourceRecordId(recordId);
        dto.setSourceBusinessId(planId);
        dto.setSourceName(planName);
        dto.setStatus(STATUS_ACTIVE);
        dto.setEffectiveTime(effectiveTime);
        dto.setExpireTime(expireTime);
        dto.setPermanent(false);
        return dto;
    }
}
