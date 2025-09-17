package org.xhy.community.domain.log.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;

/**
 * 用户活动日志仓储接口
 * 继承MyBatis Plus的BaseMapper，提供基本的CRUD操作
 */
@Mapper
public interface UserActivityLogRepository extends BaseMapper<UserActivityLogEntity> {
    
}