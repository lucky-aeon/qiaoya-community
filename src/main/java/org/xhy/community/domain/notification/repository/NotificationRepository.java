package org.xhy.community.domain.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.notification.entity.NotificationEntity;

/**
 * 通知Repository
 */
@Mapper
public interface NotificationRepository extends BaseMapper<NotificationEntity> {
    // 继承BaseMapper，使用MyBatis Plus提供的方法和条件构造器
}