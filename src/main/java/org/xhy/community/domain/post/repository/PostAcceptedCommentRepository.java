package org.xhy.community.domain.post.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.post.entity.PostAcceptedCommentEntity;

@Repository
public interface PostAcceptedCommentRepository extends BaseMapper<PostAcceptedCommentEntity> {
}

