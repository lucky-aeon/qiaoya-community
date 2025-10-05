package org.xhy.community.domain.tag.valueobject;

public enum TagAssignmentStatus {
    ISSUED,
    REVOKED,
    EXPIRED;

    public static TagAssignmentStatus fromCode(String code) {
        for (TagAssignmentStatus s : values()) {
            if (s.name().equals(code)) return s;
        }
        throw new IllegalArgumentException("Unknown tag assignment status: " + code);
    }
}

