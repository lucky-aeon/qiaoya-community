package org.xhy.community.domain.resourcebinding.repository;

import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xhy.community.domain.resourcebinding.entity.ResourceBindingEntity;

@Repository
public interface ResourceBindingRepository extends BaseMapper<ResourceBindingEntity> {
}

