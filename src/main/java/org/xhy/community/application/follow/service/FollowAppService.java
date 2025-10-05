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
import org.xhy.community.interfaces.follow.request.FollowQueryRequest;
import org.xhy.community.domain.follow.query.FollowQuery;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.user.entity.UserEntity;

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
    private final UserDomainService userDomainService;
    private final PostDomainService postDomainService;
    private final CourseDomainService courseDomainService;
    private final ChapterDomainService chapterDomainService;

    public FollowAppService(FollowDomainService followDomainService,
                            UserDomainService userDomainService,
                            PostDomainService postDomainService,
                            CourseDomainService courseDomainService,
                            ChapterDomainService chapterDomainService) {
        this.followDomainService = followDomainService;
        this.userDomainService = userDomainService;
        this.postDomainService = postDomainService;
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
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
    public IPage<FollowDTO> getMyFollowings(FollowQueryRequest request) {
        String followerId = UserContext.getCurrentUserId();

        FollowQuery query = FollowAssembler.fromRequest(request, followerId);
        IPage<FollowEntity> entityPage = followDomainService.getUserFollowings(query);

        List<FollowEntity> records = entityPage.getRecords();

        // 按类型收集目标ID，批量查询名称，避免N+1
        java.util.Set<String> userIds = new java.util.HashSet<>();
        java.util.Set<String> postIds = new java.util.HashSet<>();
        java.util.Set<String> courseIds = new java.util.HashSet<>();
        java.util.Set<String> chapterIds = new java.util.HashSet<>();

        for (FollowEntity e : records) {
            if (e.getTargetType() == null || e.getTargetId() == null) continue;
            switch (e.getTargetType()) {
                case USER -> userIds.add(e.getTargetId());
                case POST -> postIds.add(e.getTargetId());
                case COURSE -> courseIds.add(e.getTargetId());
                case CHAPTER -> chapterIds.add(e.getTargetId());
            }
        }

        java.util.Map<String, String> nameMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            // userDomainService返回实体，再取name
            java.util.Map<String, UserEntity> users =
                userDomainService.getUserEntityMapByIds(userIds);
            for (var entry : users.entrySet()) {
                nameMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().getName() : null);
            }
        }
        if (!postIds.isEmpty()) {
            nameMap.putAll(postDomainService.getPostTitleMapByIds(postIds));
        }
        if (!courseIds.isEmpty()) {
            nameMap.putAll(courseDomainService.getCourseTitleMapByIds(courseIds));
        }
        if (!chapterIds.isEmpty()) {
            nameMap.putAll(chapterDomainService.getChapterTitleMapByIds(chapterIds));
        }

        // 转换为DTO列表并填充targetName
        List<FollowDTO> dtoList = FollowAssembler.toDTOListWithTargetNames(records, nameMap);

        // 构建分页结果
        IPage<FollowDTO> dtoPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
            entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }
}
