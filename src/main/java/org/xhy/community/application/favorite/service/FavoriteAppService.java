package org.xhy.community.application.favorite.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.favorite.assembler.FavoriteAssembler;
import org.xhy.community.application.favorite.dto.FavoriteListItemDTO;
import org.xhy.community.application.favorite.dto.FavoriteStatusDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.favorite.entity.FavoriteEntity;
import org.xhy.community.domain.favorite.query.FavoriteQuery;
import org.xhy.community.domain.favorite.service.FavoriteDomainService;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.domain.interview.service.InterviewQuestionDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.favorite.request.MyFavoritesQueryRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 收藏应用服务
 * 处理用户收藏相关的业务流程
 */
@Service
public class FavoriteAppService {

    private final FavoriteDomainService favoriteDomainService;
    private final PostDomainService postDomainService;
    private final ChapterDomainService chapterDomainService;
    private final CommentDomainService commentDomainService;
    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final UserDomainService userDomainService;

    public FavoriteAppService(FavoriteDomainService favoriteDomainService,
                              PostDomainService postDomainService,
                              ChapterDomainService chapterDomainService,
                              CommentDomainService commentDomainService,
                              InterviewQuestionDomainService interviewQuestionDomainService,
                              UserDomainService userDomainService) {
        this.favoriteDomainService = favoriteDomainService;
        this.postDomainService = postDomainService;
        this.chapterDomainService = chapterDomainService;
        this.commentDomainService = commentDomainService;
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.userDomainService = userDomainService;
    }

    /**
     * 切换收藏状态
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return true=收藏成功, false=取消收藏成功
     */
    @Transactional
    public boolean toggleFavorite(String targetId, FavoriteTargetType targetType) {
        String userId = UserContext.getCurrentUserId();
        return favoriteDomainService.toggleFavorite(userId, targetId, targetType);
    }

    /**
     * 查询单个收藏状态（包含收藏数量）
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏状态DTO
     */
    public FavoriteStatusDTO getFavoriteStatus(String targetId, FavoriteTargetType targetType) {
        String userId = UserContext.getCurrentUserId();
        boolean isFavorited = favoriteDomainService.isFavorited(userId, targetId, targetType);
        long count = favoriteDomainService.countFavorites(targetId, targetType);
        return FavoriteAssembler.toFavoriteStatusDTO(targetId, targetType, isFavorited, count);
    }

    /**
     * 批量查询收藏状态（包含收藏数量）
     * @param targets Map<targetId, targetType>
     * @return 收藏状态DTO列表
     */
    public List<FavoriteStatusDTO> batchGetFavoriteStatus(Map<String, FavoriteTargetType> targets) {
        String userId = UserContext.getCurrentUserId();
        Map<String, Boolean> statusMap = favoriteDomainService.batchCheckFavoriteStatus(userId, targets);
        Map<String, Long> countMap = favoriteDomainService.batchCountFavorites(targets);
        return FavoriteAssembler.toFavoriteStatusDTOList(statusMap, countMap, targets);
    }

    /**
     * 分页查询我的收藏
     * @param request 查询请求（包含分页参数和类型筛选）
     * @return 我的收藏列表（分页）
     */
    public IPage<FavoriteListItemDTO> pageMyFavorites(MyFavoritesQueryRequest request) {
        String userId = UserContext.getCurrentUserId();

        // Request转Query
        FavoriteQuery query = FavoriteAssembler.fromRequest(request, userId);

        // 调用Domain层查询
        IPage<FavoriteEntity> entityPage = favoriteDomainService.pageMyFavorites(query);

        // 转换为DTO（基础信息）
        IPage<FavoriteListItemDTO> dtoPage = FavoriteAssembler.toListItemPage(entityPage);
        List<FavoriteListItemDTO> dtoList = dtoPage.getRecords();

        if (dtoList.isEmpty()) {
            return dtoPage;
        }

        // 批量查询扩展信息（标题、作者等）
        fillExtendedInfo(dtoList, entityPage.getRecords());

        return dtoPage;
    }

    /**
     * 填充收藏列表的扩展信息
     */
    private void fillExtendedInfo(List<FavoriteListItemDTO> dtoList, List<FavoriteEntity> entities) {
        // 按类型分组收集ID
        Set<String> postIds = new HashSet<>();
        Set<String> chapterIds = new HashSet<>();
        Set<String> commentIds = new HashSet<>();
        Set<String> questionIds = new HashSet<>();

        for (FavoriteEntity entity : entities) {
            switch (entity.getTargetType()) {
                case POST -> postIds.add(entity.getTargetId());
                case CHAPTER -> chapterIds.add(entity.getTargetId());
                case COMMENT -> commentIds.add(entity.getTargetId());
                case INTERVIEW_QUESTION -> questionIds.add(entity.getTargetId());
            }
        }

        // 批量查询各类型实体
        Map<String, org.xhy.community.domain.post.entity.PostEntity> postMap = postDomainService.getPostEntityMapByIds(postIds);
        Map<String, org.xhy.community.domain.course.entity.ChapterEntity> chapterMap = chapterDomainService.getChapterEntityMapByIds(chapterIds);
        Map<String, CommentEntity> commentMap = commentDomainService.getCommentEntityMapByIds(commentIds);
        Map<String, org.xhy.community.domain.interview.entity.InterviewQuestionEntity> questionMap = interviewQuestionDomainService.getQuestionEntityMapByIds(questionIds);

        // 收集所有作者ID
        Set<String> authorIds = new HashSet<>();
        postMap.values().forEach(post -> authorIds.add(post.getAuthorId()));
        commentMap.values().forEach(comment -> authorIds.add(comment.getCommentUserId()));
        questionMap.values().forEach(question -> authorIds.add(question.getAuthorId()));

        // 对于章节，需要查询课程获取作者
        if (!chapterMap.isEmpty()) {
            Set<String> courseIds = chapterMap.values().stream()
                    .map(org.xhy.community.domain.course.entity.ChapterEntity::getCourseId)
                    .collect(Collectors.toSet());
            // TODO: 添加批量查询课程的方法获取课程作者
            // 暂时先不填充章节的作者信息
        }

        // 批量查询作者信息
        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);

        // 填充DTO扩展信息
        for (int i = 0; i < dtoList.size(); i++) {
            FavoriteListItemDTO dto = dtoList.get(i);
            FavoriteEntity entity = entities.get(i);

            switch (entity.getTargetType()) {
                case POST -> {
                    org.xhy.community.domain.post.entity.PostEntity post = postMap.get(entity.getTargetId());
                    if (post != null) {
                        dto.setTitle(post.getTitle());
                        dto.setSnippet(post.getSummary());
                        dto.setAuthorId(post.getAuthorId());
                        UserEntity author = authorMap.get(post.getAuthorId());
                        if (author != null) {
                            dto.setAuthorName(author.getName());
                        }
                    }
                }
                case CHAPTER -> {
                    org.xhy.community.domain.course.entity.ChapterEntity chapter = chapterMap.get(entity.getTargetId());
                    if (chapter != null) {
                        dto.setTitle(chapter.getTitle());
                        // 章节暂时不填充作者信息
                    }
                }
                case COMMENT -> {
                    CommentEntity comment = commentMap.get(entity.getTargetId());
                    if (comment != null) {
                        String content = comment.getContent();
                        dto.setTitle("评论");
                        dto.setSnippet(content != null && content.length() > 100
                                ? content.substring(0, 100) + "..."
                                : content);
                        dto.setAuthorId(comment.getCommentUserId());
                        UserEntity author = authorMap.get(comment.getCommentUserId());
                        if (author != null) {
                            dto.setAuthorName(author.getName());
                        }
                        // 填充评论所属的业务对象信息
                        dto.setBusinessId(comment.getBusinessId());
                        dto.setBusinessType(comment.getBusinessType());
                    }
                }
                case INTERVIEW_QUESTION -> {
                    org.xhy.community.domain.interview.entity.InterviewQuestionEntity question = questionMap.get(entity.getTargetId());
                    if (question != null) {
                        dto.setTitle(question.getTitle());
                        dto.setAuthorId(question.getAuthorId());
                        UserEntity author = authorMap.get(question.getAuthorId());
                        if (author != null) {
                            dto.setAuthorName(author.getName());
                        }
                    }
                }
            }
        }
    }
}
