package org.xhy.community.application.course.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.course.event.CourseCompletedEvent;
import org.xhy.community.domain.tag.service.TagDomainService;

/**
 * 监听课程完成事件，发放“课程完成”标签（幂等）
 */
@Component
public class TagOnCourseCompletedListener {
    private static final Logger log = LoggerFactory.getLogger(TagOnCourseCompletedListener.class);

    private final TagDomainService tagDomainService;

    public TagOnCourseCompletedListener(TagDomainService tagDomainService) {
        this.tagDomainService = tagDomainService;
    }

    @EventListener
    public void onCourseCompleted(CourseCompletedEvent event) {
        log.info("[课程完成] 发放标签: userId={}, courseId={}, completedAt={}", event.getUserId(), event.getCourseId(), event.getCompletedAt());
        tagDomainService.issueCourseCompletionTag(event.getUserId(), event.getCourseId());
    }
}

