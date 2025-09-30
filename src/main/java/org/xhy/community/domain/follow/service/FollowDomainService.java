package org.xhy.community.domain.follow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.follow.entity.FollowEntity;
import org.xhy.community.domain.follow.query.FollowQuery;
import org.xhy.community.domain.follow.event.UserFollowedEvent;
import org.xhy.community.domain.follow.repository.FollowRepository;
import org.xhy.community.domain.follow.valueobject.FollowStatus;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.FollowErrorCode;

import java.util.List;

/**
 * 关注领域服务
 * 处理关注相关的业务逻辑
 */
@Service
public class FollowDomainService {
    
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public FollowDomainService(FollowRepository followRepository, ApplicationEventPublisher eventPublisher) {
        this.followRepository = followRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 创建关注
     */
    public FollowEntity createFollow(String followerId, String targetId, FollowTargetType targetType) {
        // 1. 检查是否自己关注自己
        if (targetType == FollowTargetType.USER && followerId.equals(targetId)) {
            throw new BusinessException(FollowErrorCode.CANNOT_FOLLOW_SELF);
        }
        
        // 2. 检查是否已经关注
        FollowEntity existingFollow = getFollowRelation(followerId, targetId, targetType);
        if (existingFollow != null) {
            if (existingFollow.isActive()) {
                throw new BusinessException(FollowErrorCode.ALREADY_FOLLOWED);
            } else {
                // 重新关注
                existingFollow.refollow();
                followRepository.updateById(existingFollow);
                
                // 发布关注事件
                eventPublisher.publishEvent(new UserFollowedEvent(followerId, targetId, targetType));
                
                return existingFollow;
            }
        }
        
        // 3. 创建新的关注关系（并发兜底：唯一约束冲突视为已关注）
        try {
            FollowEntity follow = new FollowEntity(followerId, targetId, targetType);
            followRepository.insert(follow);
            // 4. 发布关注事件
            eventPublisher.publishEvent(new UserFollowedEvent(followerId, targetId, targetType));
            return follow;
        } catch (DataIntegrityViolationException e) {
            // 并发情况下可能出现唯一约束冲突，转化为业务语义：已关注
            throw new BusinessException(FollowErrorCode.ALREADY_FOLLOWED);
        }
    }
    
    /**
     * 取消关注
     */
    public void unfollow(String followerId, String targetId, FollowTargetType targetType) {
        FollowEntity follow = getFollowRelation(followerId, targetId, targetType);
        if (follow == null || follow.isCancelled()) {
            throw new BusinessException(FollowErrorCode.NOT_FOLLOWED);
        }
        
        follow.unfollow();
        followRepository.updateById(follow);
    }
    
    /**
     * 检查是否已关注
     */
    public boolean isFollowing(String followerId, String targetId, FollowTargetType targetType) {
        FollowEntity follow = getFollowRelation(followerId, targetId, targetType);
        return follow != null && follow.isActive();
    }
    
    /**
     * 获取关注关系
     */
    public FollowEntity getFollowRelation(String followerId, String targetId, FollowTargetType targetType) {
        LambdaQueryWrapper<FollowEntity> queryWrapper = new LambdaQueryWrapper<FollowEntity>()
                .eq(FollowEntity::getFollowerId, followerId)
                .eq(FollowEntity::getTargetId, targetId)
                .eq(FollowEntity::getTargetType, targetType);
        
        return followRepository.selectOne(queryWrapper);
    }
    
    /**
     * 获取用户的关注列表（Query）
     */
    public IPage<FollowEntity> getUserFollowings(FollowQuery query) {
        Page<FollowEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<FollowEntity> queryWrapper = new LambdaQueryWrapper<FollowEntity>()
                .eq(FollowEntity::getFollowerId, query.getFollowerId())
                .eq(FollowEntity::getStatus, FollowStatus.ACTIVE)
                .eq(query.getTargetType() != null, FollowEntity::getTargetType, query.getTargetType())
                .orderByDesc(FollowEntity::getFollowTime);

        return followRepository.selectPage(page, queryWrapper);
    }
    

    
    /**
     * 获取目标对象的关注者分页列表
     */
    public IPage<FollowEntity> getFollowersPaged(String targetId, FollowTargetType targetType,
                                                Integer pageNum, Integer pageSize) {
        Page<FollowEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<FollowEntity> queryWrapper = new LambdaQueryWrapper<FollowEntity>()
                .eq(FollowEntity::getTargetId, targetId)
                .eq(FollowEntity::getTargetType, targetType)
                .eq(FollowEntity::getStatus, FollowStatus.ACTIVE)
                .orderByDesc(FollowEntity::getFollowTime);
        
        return followRepository.selectPage(page, queryWrapper);
    }
    
    /**
     * 统计关注数量
     */
    public long countFollowers(String targetId, FollowTargetType targetType) {
        LambdaQueryWrapper<FollowEntity> queryWrapper = new LambdaQueryWrapper<FollowEntity>()
                .eq(FollowEntity::getTargetId, targetId)
                .eq(FollowEntity::getTargetType, targetType)
                .eq(FollowEntity::getStatus, FollowStatus.ACTIVE);
        
        return followRepository.selectCount(queryWrapper);
    }
    
    /**
     * 统计用户关注数量
     */
    public long countFollowings(String followerId, FollowTargetType targetType) {
        LambdaQueryWrapper<FollowEntity> queryWrapper = new LambdaQueryWrapper<FollowEntity>()
                .eq(FollowEntity::getFollowerId, followerId)
                .eq(FollowEntity::getStatus, FollowStatus.ACTIVE)
                .eq(targetType != null, FollowEntity::getTargetType, targetType);
        
        return followRepository.selectCount(queryWrapper);
    }
}
