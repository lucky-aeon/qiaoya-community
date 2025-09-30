package org.xhy.community.domain.ainews.valueobject;

/**
 * AI日报条目状态
 */
public enum DailyItemStatus {
    PUBLISHED("已发布"),
    HIDDEN("隐藏");

    private final String description;

    DailyItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static DailyItemStatus fromCode(String code) {
        for (DailyItemStatus s : values()) {
            if (s.name().equalsIgnoreCase(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown DailyItemStatus: " + code);
    }
}

