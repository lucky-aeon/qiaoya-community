package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.post.entity.PostLikeEntity;
import org.xhy.community.infrastructure.exception.PostErrorCode;
import org.xhy.community.domain.post.repository.PostLikeRepository;

@Service
public class PostLikeDomainService {
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private PostDomainService postDomainService;
    
    public boolean likePost(String postId, String userId) {
        if (postDomainService.getPostById(postId) == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (isPostLikedByUser(postId, userId)) {
            throw new BusinessException(PostErrorCode.POST_ALREADY_LIKED);
        }
        
        PostLikeEntity like = new PostLikeEntity(postId, userId);
        postLikeRepository.insert(like);
        
        postDomainService.incrementLikeCount(postId);
        
        return true;
    }
    
    public boolean unlikePost(String postId, String userId) {
        if (postDomainService.getPostById(postId) == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        PostLikeEntity like = getPostLikeByUserAndPost(postId, userId);
        if (like == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_LIKED);
        }
        
        like.setDeleted(true);
        postLikeRepository.updateById(like);
        
        postDomainService.decrementLikeCount(postId);
        
        return true;
    }
    
    public boolean isPostLikedByUser(String postId, String userId) {
        return getPostLikeByUserAndPost(postId, userId) != null;
    }
    
    private PostLikeEntity getPostLikeByUserAndPost(String postId, String userId) {
        return postLikeRepository.selectOne(
            new LambdaQueryWrapper<PostLikeEntity>()
                .eq(PostLikeEntity::getPostId, postId)
                .eq(PostLikeEntity::getUserId, userId)
                .eq(PostLikeEntity::getDeleted, false)
        );
    }
}