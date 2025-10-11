package org.xhy.community.application.summary.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.summary.entity.DiscussionSummaryEntity;
import org.xhy.community.domain.summary.service.DiscussionSummaryDomainService;
import org.xhy.community.domain.summary.valueobject.SummaryMaterial;
import org.xhy.community.domain.summary.valueobject.SummaryTargetType;

import java.util.List;

@Service
public class SummaryAppService {

    private static final int MIN_COMMENT_THRESHOLD = 3; // 评论数阈值，达到后触发

    private final CommentDomainService commentDomainService;
    private final PostDomainService postDomainService;
    private final ChapterDomainService chapterDomainService;
    private final DiscussionSummaryDomainService discussionSummaryDomainService;

    public SummaryAppService(CommentDomainService commentDomainService,
                             PostDomainService postDomainService,
                             ChapterDomainService chapterDomainService,
                             DiscussionSummaryDomainService discussionSummaryDomainService) {
        this.commentDomainService = commentDomainService;
        this.postDomainService = postDomainService;
        this.chapterDomainService = chapterDomainService;
        this.discussionSummaryDomainService = discussionSummaryDomainService;
    }

    /**
     * 监听到评论创建后调用：根据评论归属（文章/章节）触发生成
     */
    public void handleCommentCreated(String commentId) {
        CommentEntity comment = commentDomainService.getCommentById(commentId);
        BusinessType bt = comment.getBusinessType();
        if (bt == BusinessType.POST || bt == BusinessType.CHAPTER) {
            generateAndSaveIfNeeded(bt == BusinessType.POST ? SummaryTargetType.POST : SummaryTargetType.CHAPTER,
                    comment.getBusinessId());
        }
    }

    /**
     * 当评论数达到阈值时生成与保存摘要
     */
    public void generateAndSaveIfNeeded(SummaryTargetType targetType, String targetId) {
        long count = getCommentCount(targetType, targetId);
        if (count <= MIN_COMMENT_THRESHOLD) {
            return; // 未达阈值，跳过
        }

        SummaryMaterial material = buildMaterial(targetType, targetId);
        String summaryText = discussionSummaryDomainService.generateSummary(material);

        DiscussionSummaryEntity entity = new DiscussionSummaryEntity();
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setSummary(summaryText);
        discussionSummaryDomainService.upsert(entity);
    }

    private long getCommentCount(SummaryTargetType type, String targetId) {
        return switch (type) {
            case POST -> commentDomainService.getCommentCountByBusiness(targetId, BusinessType.POST);
            case CHAPTER -> commentDomainService.getCommentCountByBusiness(targetId, BusinessType.CHAPTER);
        };
    }

    private SummaryMaterial buildMaterial(SummaryTargetType type, String targetId) {
        switch (type) {
            case POST: {
                PostEntity post = postDomainService.getPostById(targetId);
                List<CommentEntity> comments = commentDomainService.getCommentsByBusiness(targetId, BusinessType.POST);
                return SummaryMaterial.ofPost(post, comments);
            }
            case CHAPTER: {
                ChapterEntity chapter = chapterDomainService.getChapterById(targetId);
                List<CommentEntity> comments = commentDomainService.getCommentsByBusiness(targetId, BusinessType.CHAPTER);
                return SummaryMaterial.ofChapter(chapter, comments);
            }
            default:
                throw new IllegalArgumentException("Unsupported target type: " + type);
        }
    }
}
