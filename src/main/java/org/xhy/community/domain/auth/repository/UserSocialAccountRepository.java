package org.xhy.community.domain.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.auth.entity.UserSocialAccountEntity;

@Repository
public interface UserSocialAccountRepository extends BaseMapper<UserSocialAccountEntity> {
}

