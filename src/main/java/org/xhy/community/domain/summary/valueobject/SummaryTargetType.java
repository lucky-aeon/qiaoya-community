package org.xhy.community.domain.summary.valueobject;

/**
 * 摘要目标类型：用于指明需要生成讨论摘要的业务目标。
 * 与 Comment 的 BusinessType 解耦，便于未来独立演进。
 */
public enum SummaryTargetType {
    POST("文章"),
    CHAPTER("章节");

    private final String description;

    SummaryTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static SummaryTargetType fromCode(String code) {
        for (SummaryTargetType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown summary target type: " + code);
    }
}
