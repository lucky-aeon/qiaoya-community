package org.xhy.community.domain.oauth2.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationConsentEntity;

/**
 * OAuth2 用户授权同意 Repository
 */
@Mapper
public interface OAuth2AuthorizationConsentRepository extends BaseMapper<OAuth2AuthorizationConsentEntity> {
}
