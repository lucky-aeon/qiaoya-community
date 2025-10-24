package org.xhy.community.domain.chat.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xhy.community.domain.chat.entity.ChatMessageEntity;

@Repository
public interface ChatMessageRepository extends BaseMapper<ChatMessageEntity> {
}

