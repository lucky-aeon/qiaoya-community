package org.xhy.community.domain.read.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.common.valueobject.ReadChannel;
import org.xhy.community.infrastructure.converter.ReadChannelConverter;

import java.time.LocalDateTime;

/**
 * 用户在不同频道（文章/题目）的上次列表访问时间，用于导航小红点统计。
 */
@TableName("user_last_seen")
public class UserLastSeenEntity extends BaseEntity {

    /** 用户ID */
    private String userId;

    /** 频道（文章/题目） */
    @TableField(typeHandler = ReadChannelConverter.class)
    private ReadChannel channel;

    /** 上次访问列表时间（Last Seen） */
    private LocalDateTime lastSeenAt;

    public UserLastSeenEntity() {}

    public UserLastSeenEntity(String userId, ReadChannel channel, LocalDateTime lastSeenAt) {
        this.userId = userId;
        this.channel = channel;
        this.lastSeenAt = lastSeenAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ReadChannel getChannel() {
        return channel;
    }

    public void setChannel(ReadChannel channel) {
        this.channel = channel;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}

