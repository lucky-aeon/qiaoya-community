package org.xhy.community.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.xhy.community.application.summary.service.SummaryAppService;
import org.xhy.community.domain.common.event.ContentPublishedEvent;
import org.xhy.community.domain.common.valueobject.ContentType;

@Component
public class CommentSummaryEventListener {

    private final SummaryAppService summaryAppService;

    public CommentSummaryEventListener(SummaryAppService summaryAppService) {
        this.summaryAppService = summaryAppService;
    }

    @EventListener
    @Async
    public void onContentPublished(ContentPublishedEvent event) {
        if (event.getContentType() != ContentType.COMMENT) return;
        // 仅对评论创建事件处理，交由应用服务编排
        summaryAppService.handleCommentCreated(event.getContentId());
    }
}
