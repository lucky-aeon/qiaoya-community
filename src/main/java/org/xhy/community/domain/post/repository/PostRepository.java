package org.xhy.community.domain.post.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.post.entity.PostEntity;

@Repository
public interface PostRepository extends BaseMapper<PostEntity> {
}