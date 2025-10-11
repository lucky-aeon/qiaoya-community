package org.xhy.community.domain.summary.valueobject;

import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.post.entity.PostEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 生成摘要所需的素材：目标类型、目标ID、标题、正文、评论列表。
 * 作为不可变值对象在应用层聚合后传入领域服务，便于扩展新的目标类型。
 */
public final class SummaryMaterial {

    private final SummaryTargetType targetType;
    private final String targetId;
    private final String title;
    private final String content;
    private final List<CommentEntity> comments;

    private SummaryMaterial(SummaryTargetType targetType,
                            String targetId,
                            String title,
                            String content,
                            List<CommentEntity> comments) {
        this.targetType = Objects.requireNonNull(targetType, "targetType");
        this.targetId = Objects.requireNonNull(targetId, "targetId");
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.comments = comments == null ? Collections.emptyList() : comments;
    }

    public static SummaryMaterial ofPost(PostEntity post, List<CommentEntity> comments) {
        return new SummaryMaterial(
                SummaryTargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getContent(),
                comments
        );
    }

    public static SummaryMaterial ofChapter(ChapterEntity chapter, List<CommentEntity> comments) {
        return new SummaryMaterial(
                SummaryTargetType.CHAPTER,
                chapter.getId(),
                chapter.getTitle(),
                chapter.getContent(),
                comments
        );
    }

    public SummaryTargetType getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<CommentEntity> getComments() { return comments; }
}

