package org.xhy.community.domain.updatelog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.updatelog.entity.UpdateLogEntity;

@Repository
public interface UpdateLogRepository extends BaseMapper<UpdateLogEntity> {
}