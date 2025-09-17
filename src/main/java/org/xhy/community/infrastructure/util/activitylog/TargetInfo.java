package org.xhy.community.infrastructure.util.activitylog;

/**
 * 目标信息封装类
 * 用于封装从URL中解析出的目标类型和目标ID
 */
public class TargetInfo {
    
    /**
     * 目标类型（如POST、COURSE、USER等）
     */
    private final String type;
    
    /**
     * 目标ID
     */
    private final String id;
    
    public TargetInfo(String type, String id) {
        this.type = type;
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return "TargetInfo{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}