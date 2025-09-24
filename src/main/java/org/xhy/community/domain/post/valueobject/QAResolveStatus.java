package org.xhy.community.domain.post.valueobject;

/**
 * 问答帖解决状态
 */
public enum QAResolveStatus {
    UNSOLVED("未解决"),
    SOLVED("已解决");

    private final String description;

    QAResolveStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static QAResolveStatus fromCode(String code) {
        for (QAResolveStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown QAResolveStatus code: " + code);
    }
}

