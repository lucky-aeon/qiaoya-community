package org.xhy.community.application.follow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.follow.assembler.FollowAssembler;
import org.xhy.community.application.follow.dto.FollowDTO;
import org.xhy.community.application.follow.dto.FollowStatisticsDTO;
import org.xhy.community.domain.follow.entity.FollowEntity;
import org.xhy.community.domain.follow.service.FollowDomainService;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.infrastructure.config.UserContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注应用服务
 * 处理用户关注相关的业务流程
 */
@Service
public class FollowAppService {
    
    private final FollowDomainService followDomainService;
    
    public FollowAppService(FollowDomainService followDomainService) {
        this.followDomainService = followDomainService;
    }
    
    /**
     * 创建关注
     */
    @Transactional
    public FollowDTO follow(String targetId, FollowTargetType targetType) {
        String followerId = UserContext.getCurrentUserId();
        
        FollowEntity follow = followDomainService.createFollow(followerId, targetId, targetType);
        
        return FollowAssembler.toDTO(follow);
    }
    
    /**
     * 取消关注
     */
    @Transactional
    public void unfollow(String targetId, FollowTargetType targetType) {
        String followerId = UserContext.getCurrentUserId();
        
        followDomainService.unfollow(followerId, targetId, targetType);
    }
    
    /**
     * 检查是否已关注
     */
    public boolean checkFollowStatus(String targetId, FollowTargetType targetType) {
        String followerId = UserContext.getCurrentUserId();
        
        return followDomainService.isFollowing(followerId, targetId, targetType);
    }
    
    /**
     * 获取我的关注列表
     */
    public IPage<FollowDTO> getMyFollowings(FollowTargetType targetType, Integer pageNum, Integer pageSize) {
        String followerId = UserContext.getCurrentUserId();
        
        IPage<FollowEntity> entityPage = followDomainService.getUserFollowings(
            followerId, targetType, pageNum, pageSize);
        
        // 基础转换
        List<FollowDTO> dtoList = FollowAssembler.toDTOList(entityPage.getRecords());
        
        // 构建分页结果
        IPage<FollowDTO> dtoPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
            entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 获取目标对象的关注者列表
     */
    public IPage<FollowDTO> getFollowers(String targetId, FollowTargetType targetType, 
                                        Integer pageNum, Integer pageSize) {
        IPage<FollowEntity> entityPage = followDomainService.getFollowersPaged(
            targetId, targetType, pageNum, pageSize);
        
        // 基础转换
        List<FollowDTO> dtoList = FollowAssembler.toDTOList(entityPage.getRecords());
        
        // 构建分页结果
        IPage<FollowDTO> dtoPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
            entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 获取关注统计信息
     */
    public FollowStatisticsDTO getFollowStatistics(String targetId, FollowTargetType targetType) {
        FollowStatisticsDTO statistics = new FollowStatisticsDTO();
        
        if (targetId != null && targetType != null) {
            // 查询特定目标的关注者数量
            long followersCount = followDomainService.countFollowers(targetId, targetType);
            statistics.setTotalFollowers(followersCount);
            statistics.setTargetId(targetId);
            statistics.setTargetType(targetType);
        }
        
        return statistics;
    }
    
    /**
     * 获取用户关注统计信息
     */
    public FollowStatisticsDTO getUserFollowStatistics(String userId) {
        if (userId == null) {
            userId = UserContext.getCurrentUserId();
        }
        
        FollowStatisticsDTO statistics = new FollowStatisticsDTO();
        statistics.setUserId(userId);
        
        // 统计各类型的关注数量
        Map<FollowTargetType, Long> followingsByType = new HashMap<>();
        for (FollowTargetType type : FollowTargetType.values()) {
            long count = followDomainService.countFollowings(userId, type);
            followingsByType.put(type, count);
        }
        
        // 计算总关注数
        long totalFollowings = followingsByType.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        statistics.setTotalFollowings(totalFollowings);
        statistics.setFollowingsByType(followingsByType);
        
        return statistics;
    }
    
    /**
     * 批量检查关注状态
     */
    public Map<String, Boolean> batchCheckFollowStatus(List<String> targetIds, FollowTargetType targetType) {
        String followerId = UserContext.getCurrentUserId();
        Map<String, Boolean> statusMap = new HashMap<>();
        
        for (String targetId : targetIds) {
            boolean isFollowing = followDomainService.isFollowing(followerId, targetId, targetType);
            statusMap.put(targetId, isFollowing);
        }
        
        return statusMap;
    }
}