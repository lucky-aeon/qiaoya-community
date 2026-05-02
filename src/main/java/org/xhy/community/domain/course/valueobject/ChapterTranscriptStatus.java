package org.xhy.community.domain.course.valueobject;

public enum ChapterTranscriptStatus {
    PENDING("等待提交"),
    SUBMITTED("已提交"),
    RUNNING("处理中"),
    SUCCEEDED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String description;

    ChapterTranscriptStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ChapterTranscriptStatus fromCode(String code) {
        for (ChapterTranscriptStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown chapter transcript status: " + code);
    }
}
