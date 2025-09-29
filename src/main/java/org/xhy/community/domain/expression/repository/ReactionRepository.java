package org.xhy.community.domain.expression.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.expression.entity.ReactionEntity;

@Repository
public interface ReactionRepository extends BaseMapper<ReactionEntity> {
}

