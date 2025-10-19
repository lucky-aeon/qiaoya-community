package org.xhy.community.domain.interview.valueobject;

/**
 * 面试题发布状态
 */
public enum ProblemStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    ARCHIVED("已归档");

    private final String description;

    ProblemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ProblemStatus fromCode(String code) {
        for (ProblemStatus s : values()) {
            if (s.name().equals(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown ProblemStatus code: " + code);
    }
}

