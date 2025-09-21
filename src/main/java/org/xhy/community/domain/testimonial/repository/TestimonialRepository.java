package org.xhy.community.domain.testimonial.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;

@Repository
public interface TestimonialRepository extends BaseMapper<TestimonialEntity> {
}