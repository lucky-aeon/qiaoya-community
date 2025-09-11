package org.xhy.community.domain.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.user.entity.UserEntity;

@Repository
public interface UserRepository extends BaseMapper<UserEntity> {
}