package org.xhy.community.infrastructure.util.activitylog;

/**
 * 目标类型映射配置类
 * 用于配置URL模式到目标类型的映射关系
 */
public class TargetTypeMapping {
    
    /**
     * 目标类型名称
     */
    private final String targetType;
    
    /**
     * 正则表达式中ID所在的组索引
     */
    private final int idGroupIndex;
    
    public TargetTypeMapping(String targetType, int idGroupIndex) {
        this.targetType = targetType;
        this.idGroupIndex = idGroupIndex;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public int getIdGroupIndex() {
        return idGroupIndex;
    }
    
    @Override
    public String toString() {
        return "TargetTypeMapping{" +
                "targetType='" + targetType + '\'' +
                ", idGroupIndex=" + idGroupIndex +
                '}';
    }
}