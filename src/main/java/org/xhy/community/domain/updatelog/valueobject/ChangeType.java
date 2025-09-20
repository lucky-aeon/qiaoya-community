package org.xhy.community.domain.updatelog.valueobject;

public enum ChangeType {
    FEATURE("新功能"),
    IMPROVEMENT("改进"),
    BUGFIX("修复"),
    SECURITY("安全"),
    BREAKING("破坏性变更");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ChangeType fromCode(String code) {
        for (ChangeType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown change type code: " + code);
    }
}