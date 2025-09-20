package org.xhy.community.domain.updatelog.valueobject;

public enum UpdateLogStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布");

    private final String description;

    UpdateLogStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static UpdateLogStatus fromCode(String code) {
        for (UpdateLogStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown update log status code: " + code);
    }
}