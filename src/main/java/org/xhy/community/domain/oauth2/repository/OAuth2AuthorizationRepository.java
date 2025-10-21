package org.xhy.community.domain.oauth2.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;

/**
 * OAuth2 授权记录 Repository
 */
@Mapper
public interface OAuth2AuthorizationRepository extends BaseMapper<OAuth2AuthorizationEntity> {
}
