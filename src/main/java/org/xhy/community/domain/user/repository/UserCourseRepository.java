package org.xhy.community.domain.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xhy.community.domain.user.entity.UserCourseEntity;

/**
 * 用户课程权限仓储接口
 */
@Mapper
public interface UserCourseRepository extends BaseMapper<UserCourseEntity> {
    
    // 继承BaseMapper提供的基础CRUD操作
    // 可以使用LambdaQueryWrapper进行条件查询
}