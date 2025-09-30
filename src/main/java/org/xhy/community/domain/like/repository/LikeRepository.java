package org.xhy.community.domain.like.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.like.entity.LikeEntity;

/**
 * 点赞仓储接口
 * 直接继承MyBatis Plus的BaseMapper，使用条件构造器进行查询
 */
@Repository
public interface LikeRepository extends BaseMapper<LikeEntity> {

    // 使用MyBatis Plus提供的方法和条件构造器
    // 不需要写自定义SQL方法
}