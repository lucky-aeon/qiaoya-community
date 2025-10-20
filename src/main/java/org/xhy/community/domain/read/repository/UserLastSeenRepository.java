package org.xhy.community.domain.read.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.read.entity.UserLastSeenEntity;

@Repository
public interface UserLastSeenRepository extends BaseMapper<UserLastSeenEntity> {
}

