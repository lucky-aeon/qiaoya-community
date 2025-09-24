package org.xhy.community.application.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.post.assembler.AdminPostAssembler;
import org.xhy.community.application.post.dto.AdminPostDTO;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.query.PostQuery;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.post.request.AdminPostQueryRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员文章应用服务
 * 提供管理员对所有文章的查询功能
 */
@Service
public class AdminPostAppService {
    
    private final PostDomainService postDomainService;
    private final UserDomainService userDomainService;
    private final CategoryDomainService categoryDomainService;
    
    public AdminPostAppService(PostDomainService postDomainService,
                              UserDomainService userDomainService,
                              CategoryDomainService categoryDomainService) {
        this.postDomainService = postDomainService;
        this.userDomainService = userDomainService;
        this.categoryDomainService = categoryDomainService;
    }
    
    /**
     * 管理员分页查询所有文章
     * 查询所有用户的文章，包含作者名称和分类名称
     * 
     * @param request 查询请求参数，包含分页信息
     * @return 包含完整信息的文章分页列表
     */
    public IPage<AdminPostDTO> getAdminPosts(AdminPostQueryRequest request) {
        // 使用管理员权限查询所有文章
        PostQuery query = AdminPostAssembler.fromRequest(request);
        IPage<PostEntity> entityPage = postDomainService.queryPosts(query);
        
        List<PostEntity> posts = entityPage.getRecords();
        if (posts.isEmpty()) {
            Page<AdminPostDTO> emptyPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            emptyPage.setRecords(java.util.Collections.emptyList());
            return emptyPage;
        }
        
        // 收集所有的authorId和categoryId
        Set<String> authorIds = posts.stream()
                .map(PostEntity::getAuthorId)
                .collect(Collectors.toSet());
        
        Set<String> categoryIds = posts.stream()
                .map(PostEntity::getCategoryId)
                .collect(Collectors.toSet());
        
        // 批量查询用户和分类信息
        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);
        List<CategoryEntity> categories = categoryDomainService.getCategoriesByIds(categoryIds);
        
        // 转换为Map便于查找
        Map<String, String> categoryNames = categories.stream()
                .collect(Collectors.toMap(
                    CategoryEntity::getId,
                    CategoryEntity::getName
                ));
        
        // 组装AdminPostDTO
        List<AdminPostDTO> dtoList = AdminPostAssembler.toDTOList(posts, authorMap, categoryNames);
        
        Page<AdminPostDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public PostDTO forceAcceptComment(String postId, String commentId, String adminId) {
        PostEntity post = postDomainService.acceptComment(postId, commentId, adminId, AccessLevel.ADMIN);
        return org.xhy.community.application.post.assembler.PostAssembler.toDTO(post);
    }

    @Transactional(rollbackFor = Exception.class)
    public PostDTO forceRevokeAcceptance(String postId, String commentId, String adminId) {
        PostEntity post = postDomainService.revokeAcceptance(postId, commentId, adminId, AccessLevel.ADMIN);
        return org.xhy.community.application.post.assembler.PostAssembler.toDTO(post);
    }
}
