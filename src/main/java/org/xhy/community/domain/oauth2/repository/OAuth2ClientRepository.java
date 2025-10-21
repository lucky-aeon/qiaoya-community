package org.xhy.community.domain.oauth2.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;

/**
 * OAuth2 客户端仓储接口
 * 继承 MyBatis Plus BaseMapper，使用条件构造器进行查询
 */
@Mapper
public interface OAuth2ClientRepository extends BaseMapper<OAuth2ClientEntity> {
    // 继承 BaseMapper，使用 MyBatis Plus 提供的方法
    // 不需要编写自定义 SQL
}
