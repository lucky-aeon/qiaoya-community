package org.xhy.community.domain.favorite.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.favorite.entity.FavoriteEntity;

@Mapper
public interface FavoriteRepository extends BaseMapper<FavoriteEntity> {
}
