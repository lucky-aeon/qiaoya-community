package org.xhy.community.domain.follow.event;

import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一的内容更新事件
 * 当被关注的内容发生更新时触发
 */
public class ContentUpdatedEvent {
    
    /** 被更新内容的ID */
    private final String targetId;
    
    /** 内容类型 */
    private final FollowTargetType targetType;
    
    /** 内容作者/创建者ID */
    private final String authorId;
    
    /** 更新时间 */
    private final LocalDateTime updateTime;
    
    /** 扩展信息（用于存储特定类型的额外数据） */
    private final Map<String, Object> extraData;
    
    public ContentUpdatedEvent(String targetId, FollowTargetType targetType, String authorId, 
                              LocalDateTime updateTime, Map<String, Object> extraData) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.authorId = authorId;
        this.updateTime = updateTime;
        this.extraData = extraData;
    }
    
    public String getTargetId() { return targetId; }
    public FollowTargetType getTargetType() { return targetType; }
    public String getAuthorId() { return authorId; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Map<String, Object> getExtraData() { return extraData; }
    
    /**
     * 获取扩展数据中的特定值
     */
    public Object getExtraData(String key) {
        return extraData != null ? extraData.get(key) : null;
    }
    
    /**
     * 获取更新类型
     */
    public String getUpdateType() {
        return extraData != null ? (String) extraData.get("updateType") : null;
    }
    
    /**
     * 获取更新描述
     */
    public String getDescription() {
        return extraData != null ? (String) extraData.get("description") : null;
    }
}