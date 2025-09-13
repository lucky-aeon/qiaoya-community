package org.xhy.community.domain.resource.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.resource.entity.ResourceEntity;

@Mapper
public interface ResourceRepository extends BaseMapper<ResourceEntity> {
    
}