package org.xhy.community.domain.comment.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.comment.entity.CommentEntity;

@Repository
public interface CommentRepository extends BaseMapper<CommentEntity> {
}