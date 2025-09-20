package org.xhy.community.domain.config.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.config.entity.SystemConfigEntity;

@Repository
public interface SystemConfigRepository extends BaseMapper<SystemConfigEntity> {
}