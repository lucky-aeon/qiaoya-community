package org.xhy.community.domain.ainews.valueobject;

/**
 * AI日报来源
 */
public enum DailySource {
    AIBASE;

    public static DailySource fromCode(String code) {
        for (DailySource s : values()) {
            if (s.name().equalsIgnoreCase(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown DailySource: " + code);
    }
}

