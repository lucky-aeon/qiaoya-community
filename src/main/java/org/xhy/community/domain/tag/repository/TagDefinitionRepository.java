package org.xhy.community.domain.tag.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;

@Mapper
public interface TagDefinitionRepository extends BaseMapper<TagDefinitionEntity> {
}

