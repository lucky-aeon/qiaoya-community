package org.xhy.community.application.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.post.assembler.PostAssembler;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.interfaces.post.request.CreatePostRequest;
import org.xhy.community.interfaces.post.request.PostQueryRequest;
import org.xhy.community.interfaces.post.request.UpdatePostRequest;

import java.util.List;

@Service
public class PostAppService {
    
    private final PostDomainService postDomainService;
    
    public PostAppService(PostDomainService postDomainService) {
        this.postDomainService = postDomainService;
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
}