package org.xhy.community.domain.updatelog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.updatelog.entity.UpdateLogChangeEntity;

@Repository
public interface UpdateLogChangeRepository extends BaseMapper<UpdateLogChangeEntity> {
}