package org.xhy.community.domain.skill.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.skill.entity.SkillEntity;

@Mapper
@Repository
public interface SkillRepository extends BaseMapper<SkillEntity> {
}
