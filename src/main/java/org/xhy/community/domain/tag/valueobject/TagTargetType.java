package org.xhy.community.domain.tag.valueobject;

/**
 * 标签绑定的目标对象类型
 */
public enum TagTargetType {
    COURSE,
    CHAPTER,
    POST,
    ACTIVITY;

    public static TagTargetType fromCode(String code) {
        for (TagTargetType t : values()) {
            if (t.name().equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown TagTargetType: " + code);
    }
}

