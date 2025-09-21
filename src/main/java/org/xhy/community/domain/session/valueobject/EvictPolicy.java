package org.xhy.community.domain.session.valueobject;

/**
 * 超出并发活跃 IP 配额时的处理策略。
 */
public enum EvictPolicy {
    /** 拒绝新登录 */
    DENY_NEW,
    /** 淘汰最久未活跃 IP，再接受新 IP */
    EVICT_OLDEST
}

