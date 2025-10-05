package org.xhy.community.domain.course.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.course.entity.UserChapterProgressEntity;

@Mapper
public interface UserChapterProgressRepository extends BaseMapper<UserChapterProgressEntity> {
}

