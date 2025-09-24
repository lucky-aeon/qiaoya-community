package org.xhy.community.application.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.post.assembler.FrontPostDetailAssembler;
import org.xhy.community.application.post.assembler.FrontPostAssembler;
import org.xhy.community.application.post.assembler.PostAssembler;
import org.xhy.community.application.post.assembler.PublicPostAssembler;
import org.xhy.community.application.post.dto.FrontPostDetailDTO;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.application.post.dto.PublicPostDTO;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.query.PostQuery;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.interfaces.post.request.CreatePostRequest;
import org.xhy.community.interfaces.post.request.PostQueryRequest;
import org.xhy.community.interfaces.post.request.AppPostQueryRequest;
import org.xhy.community.interfaces.post.request.UpdatePostRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PostAppService {
    
    private final PostDomainService postDomainService;
    private final UserDomainService userDomainService;
    private final CategoryDomainService categoryDomainService;
    
    public PostAppService(PostDomainService postDomainService, 
                         UserDomainService userDomainService,
                         CategoryDomainService categoryDomainService) {
        this.postDomainService = postDomainService;
        this.userDomainService = userDomainService;
        this.categoryDomainService = categoryDomainService;
    }
    
    public PostDTO createPost(CreatePostRequest request, String authorId) {
        // 通过Assembler将Request转换为Entity
        PostEntity post = PostAssembler.fromCreateRequest(request, authorId);
        
        // 调用Domain层进行业务处理和保存
        PostEntity createdPost = postDomainService.createPost(post);
        PostDTO dto = PostAssembler.toDTO(createdPost);
        CategoryEntity category = categoryDomainService.getCategoryById(createdPost.getCategoryId());
        if (category != null) {
            dto.setCategoryType(category.getType());
        }
        return dto;
    }
    
    public PostDTO updatePost(String postId, UpdatePostRequest request, String authorId) {

        // 通过Assembler更新Entity
        PostEntity postEntity = PostAssembler.updateEntityFromRequest(postId, request);

        // 调用Domain层进行更新（包含权限校验和分类验证）
        PostEntity updatedPost = postDomainService.updatePost(postEntity, authorId);
        PostDTO dto = PostAssembler.toDTO(updatedPost);
        CategoryEntity category = categoryDomainService.getCategoryById(updatedPost.getCategoryId());
        if (category != null) {
            dto.setCategoryType(category.getType());
        }
        return dto;
    }
    
    public PostDTO getPostById(String postId, String currentUserId) {
        // Domain层已经处理了所有异常情况，直接转换即可
        PostEntity post = postDomainService.getUserPostById(postId, currentUserId);
        PostDTO dto = PostAssembler.toDTO(post);
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category != null) {
            dto.setCategoryType(category.getType());
        }
        return dto;
    }
    
    public IPage<PostDTO> getUserPosts(String authorId, PostQueryRequest request) {
        PostQuery query = PostAssembler.fromUserRequest(request, authorId);
        IPage<PostEntity> entityPage = postDomainService.queryPosts(query);
        
        // 转换为DTO分页结果
        Page<PostDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<PostEntity> records = entityPage.getRecords();
        List<PostDTO> dtoList = PostAssembler.toDTOList(records);
        if (!records.isEmpty()) {
            java.util.Set<String> categoryIds = records.stream().map(PostEntity::getCategoryId).collect(java.util.stream.Collectors.toSet());
            List<CategoryEntity> categories = categoryDomainService.getCategoriesByIds(categoryIds);
            java.util.Map<String, org.xhy.community.domain.post.valueobject.CategoryType> typeMap = categories.stream()
                    .collect(java.util.stream.Collectors.toMap(CategoryEntity::getId, CategoryEntity::getType));
            for (int i = 0; i < records.size(); i++) {
                PostEntity pe = records.get(i);
                PostDTO dto = dtoList.get(i);
                dto.setCategoryType(typeMap.get(pe.getCategoryId()));
            }
        }
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(String postId, String authorId) {
        // 先清理采纳关系，再删除文章
        postDomainService.removeAllAcceptancesByPostId(postId);
        postDomainService.deletePost(postId, authorId);
    }
    
    public PostDTO changePostStatus(String postId, PostStatus targetStatus, String authorId) {
        // Domain层处理不同状态的业务逻辑
        PostEntity updatedPost;
        if (targetStatus == PostStatus.PUBLISHED) {
            updatedPost = postDomainService.publishPost(postId, authorId);
        } else if (targetStatus == PostStatus.DRAFT) {
            updatedPost = postDomainService.unpublishPost(postId, authorId);
        } else {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "不支持的状态变更");
        }
        
        return PostAssembler.toDTO(updatedPost);
    }
    
    public IPage<FrontPostDTO> queryAppPosts(AppPostQueryRequest request) {
        PostQuery query = PostAssembler.fromAppRequest(request);
        IPage<PostEntity> entityPage = postDomainService.queryAppPosts(query);
        
        List<PostEntity> posts = entityPage.getRecords();
        if (posts.isEmpty()) {
            Page<FrontPostDTO> emptyPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            emptyPage.setRecords(java.util.Collections.emptyList());
            return emptyPage;
        }
        
        // 收集所有的authorId和categoryId
        java.util.Set<String> authorIds = posts.stream()
                .map(PostEntity::getAuthorId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<String> categoryIds = posts.stream()
                .map(PostEntity::getCategoryId)
                .collect(java.util.stream.Collectors.toSet());
        
        // 批量查询用户和分类信息
        java.util.Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);
        List<CategoryEntity> categories = categoryDomainService.getCategoriesByIds(categoryIds);
        
        // 转换为Map便于查找
        java.util.Map<String, String> categoryNames = categories.stream()
                .collect(java.util.stream.Collectors.toMap(
                    CategoryEntity::getId,
                    CategoryEntity::getName
                ));
        java.util.Map<String, org.xhy.community.domain.post.valueobject.CategoryType> categoryTypes = categories.stream()
                .collect(java.util.stream.Collectors.toMap(
                    CategoryEntity::getId,
                    CategoryEntity::getType
                ));
        
        // 组装FrontPostDTO
        List<FrontPostDTO> dtoList = posts.stream()
                .map(post -> {
                    FrontPostDTO dto = FrontPostAssembler.toDTO(post, authorMap, categoryNames);
                    dto.setCategoryType(categoryTypes.get(post.getCategoryId()));
                    return dto;
                })
                .toList();
        
        Page<FrontPostDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 获取用户前台文章详情
     * 只能查看已发布的文章
     * 
     * @param postId 文章ID
     * @return 文章详细信息，包含完整内容
     * @throws BusinessException 如果文章不存在或未发布
     */
    public FrontPostDetailDTO getAppPostDetail(String postId) {
        PostEntity post = postDomainService.getAppPostById(postId);
        
        // 获取作者信息
        Set<String> authorIds = java.util.Collections.singleton(post.getAuthorId());
        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);
        UserEntity author = authorMap.get(post.getAuthorId());
        
        // 获取分类信息
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        String categoryName = category != null ? category.getName() : null;
        
        FrontPostDetailDTO dto = FrontPostDetailAssembler.toDTO(post, author, categoryName);
        if (category != null) {
            dto.setCategoryType(category.getType());
        }
        // 问答帖子返回采纳评论ID集合，供前台渲染
        if (category != null && category.getType() == org.xhy.community.domain.post.valueobject.CategoryType.QA) {
            java.util.Set<String> ids = postDomainService.getAcceptedCommentIds(post.getId());
            dto.setAcceptedCommentIds(new java.util.ArrayList<>(ids));
        }
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public PostDTO acceptComment(String postId, String commentId, String operatorId) {
        PostEntity post = postDomainService.acceptComment(postId, commentId, operatorId, AccessLevel.USER);
        PostDTO dto = PostAssembler.toDTO(post);
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category != null) {
            dto.setCategoryType(category.getType());
        }
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public PostDTO revokeAcceptance(String postId, String commentId, String operatorId) {
        PostEntity post = postDomainService.revokeAcceptance(postId, commentId, operatorId, AccessLevel.USER);
        PostDTO dto = PostAssembler.toDTO(post);
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category != null) {
            dto.setCategoryType(category.getType());
        }
        return dto;
    }
}
