package org.xhy.community.domain.expression.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.expression.entity.ExpressionTypeEntity;

@Repository
public interface ExpressionTypeRepository extends BaseMapper<ExpressionTypeEntity> {
}

