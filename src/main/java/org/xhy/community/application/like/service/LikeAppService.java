package org.xhy.community.application.like.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.like.assembler.LikeAssembler;
import org.xhy.community.application.like.dto.LikeCountDTO;
import org.xhy.community.application.like.dto.LikeStatusDTO;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.infrastructure.config.UserContext;

import java.util.List;
import java.util.Map;

/**
 * 点赞应用服务
 * 处理用户点赞相关的业务流程
 */
@Service
public class LikeAppService {

    private final LikeDomainService likeDomainService;

    public LikeAppService(LikeDomainService likeDomainService) {
        this.likeDomainService = likeDomainService;
    }

    /**
     * 切换点赞状态
     * @return true=点赞成功, false=取消点赞成功
     */
    @Transactional
    public boolean toggleLike(String targetId, LikeTargetType targetType) {
        String userId = UserContext.getCurrentUserId();
        return likeDomainService.toggleLike(userId, targetId, targetType);
    }

    /**
     * 查询单个点赞状态
     */
    public LikeStatusDTO getLikeStatus(String targetId, LikeTargetType targetType) {
        String userId = UserContext.getCurrentUserId();
        boolean isLiked = likeDomainService.isLiked(userId, targetId, targetType);
        return LikeAssembler.toLikeStatusDTO(targetId, targetType, isLiked);
    }

    /**
     * 批量查询点赞状态
     * @param targets Map<targetId, targetType>
     */
    public List<LikeStatusDTO> batchGetLikeStatus(Map<String, LikeTargetType> targets) {
        String userId = UserContext.getCurrentUserId();
        Map<String, Boolean> statusMap = likeDomainService.batchCheckLikeStatus(userId, targets);
        return LikeAssembler.toLikeStatusDTOList(statusMap, targets);
    }

    /**
     * 统计单个点赞数
     */
    public LikeCountDTO getLikeCount(String targetId, LikeTargetType targetType) {
        long count = likeDomainService.countLikes(targetId, targetType);
        return LikeAssembler.toLikeCountDTO(targetId, targetType, count);
    }

    /**
     * 批量统计点赞数
     * @param targets Map<targetId, targetType>
     */
    public List<LikeCountDTO> batchGetLikeCount(Map<String, LikeTargetType> targets) {
        Map<String, Long> countMap = likeDomainService.batchCountLikes(targets);
        return LikeAssembler.toLikeCountDTOList(countMap, targets);
    }
}