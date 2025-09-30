package org.xhy.community.domain.ainews.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;

@Mapper
@Repository
public interface DailyItemRepository extends BaseMapper<DailyItemEntity> {
}

