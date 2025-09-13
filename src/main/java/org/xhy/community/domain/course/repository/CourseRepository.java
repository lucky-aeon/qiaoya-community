package org.xhy.community.domain.course.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.course.entity.CourseEntity;

@Repository
public interface CourseRepository extends BaseMapper<CourseEntity> {
}