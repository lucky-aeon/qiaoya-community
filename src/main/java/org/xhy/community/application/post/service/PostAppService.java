package org.xhy.community.application.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.post.assembler.FrontPostAssembler;
import org.xhy.community.application.post.assembler.PostAssembler;
import org.xhy.community.application.post.assembler.PublicPostAssembler;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.application.post.dto.PublicPostDTO;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.interfaces.post.request.CreatePostRequest;
import org.xhy.community.interfaces.post.request.PostQueryRequest;
import org.xhy.community.interfaces.post.request.PublicPostQueryRequest;
import org.xhy.community.interfaces.post.request.UpdatePostRequest;

import java.util.List;

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
        
        return PostAssembler.toDTO(createdPost);
    }
    
    public PostDTO updatePost(String postId, UpdatePostRequest request, String authorId) {
        // 获取现有文章进行权限校验和基础信息获取
        PostEntity existingPost = postDomainService.getUserPostById(postId, authorId);
        
        // 通过Assembler更新Entity
        PostAssembler.updateEntityFromRequest(existingPost, request);
        
        // 调用Domain层进行更新（包含权限校验和分类验证）
        PostEntity updatedPost = postDomainService.updatePost(existingPost, authorId);
        
        return PostAssembler.toDTO(updatedPost);
    }
    
    public PostDTO getPostById(String postId, String currentUserId) {
        // Domain层已经处理了所有异常情况，直接转换即可
        PostEntity post = postDomainService.getUserPostById(postId, currentUserId);
        return PostAssembler.toDTO(post);
    }
    
    public IPage<PostDTO> getUserPosts(String authorId, PostQueryRequest request) {
        IPage<PostEntity> entityPage = postDomainService.getUserPosts(
            authorId, 
            request.getPageNum(), 
            request.getPageSize(), 
            request.getStatus()
        );
        
        // 转换为DTO分页结果
        Page<PostDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<PostDTO> dtoList = PostAssembler.toDTOList(entityPage.getRecords());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    public void deletePost(String postId, String authorId) {
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
    
    public IPage<FrontPostDTO> queryPublicPosts(PublicPostQueryRequest request) {
        IPage<PostEntity> entityPage = postDomainService.queryPublicPosts(
            request.getPage(),
            request.getSize(),
            request.getCategoryType()
        );
        
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
        List<UserEntity> users = userDomainService.getUsersByIds(authorIds);
        List<CategoryEntity> categories = categoryDomainService.getCategoriesByIds(categoryIds);
        
        // 转换为Map便于查找
        java.util.Map<String, String> authorNames = users.stream()
                .collect(java.util.stream.Collectors.toMap(
                    UserEntity::getId,
                    UserEntity::getName
                ));
        
        java.util.Map<String, String> categoryNames = categories.stream()
                .collect(java.util.stream.Collectors.toMap(
                    CategoryEntity::getId,
                    CategoryEntity::getName
                ));
        
        // 组装FrontPostDTO
        List<FrontPostDTO> dtoList = posts.stream()
                .map(post -> FrontPostAssembler.toDTO(post, authorNames, categoryNames))
                .toList();
        
        Page<FrontPostDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
}