package org.xhy.community.domain.testimonial.valueobject;

public enum TestimonialStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    PUBLISHED("已发布");

    private final String description;

    TestimonialStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static TestimonialStatus fromCode(String code) {
        for (TestimonialStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown testimonial status code: " + code);
    }
}