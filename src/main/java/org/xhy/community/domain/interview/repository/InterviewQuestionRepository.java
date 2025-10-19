package org.xhy.community.domain.interview.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;

@Repository
public interface InterviewQuestionRepository extends BaseMapper<InterviewQuestionEntity> {
}

