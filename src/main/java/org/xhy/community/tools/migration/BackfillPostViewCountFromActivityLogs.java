package org.xhy.community.tools.migration;

import java.sql.*;

/**
 * 从 user_activity_logs 回填 posts.view_count
 * 口径：统计所有 VIEW_POST 事件（非去重），与新系统运行时“每次浏览+1”一致
 * 幂等：直接 UPDATE 按聚合结果写入
 */
public class BackfillPostViewCountFromActivityLogs {
    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[BackfillPostViewCount] Postgres -> " + pgUrl);
        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            String upsert = "UPDATE posts p SET view_count = COALESCE(v.cnt,0), update_time = now() " +
                    "FROM (SELECT target_id, COUNT(*) AS cnt FROM user_activity_logs " +
                    "WHERE activity_type='VIEW_POST' AND target_type='POST' GROUP BY target_id) v " +
                    "WHERE p.id = v.target_id";
            try (PreparedStatement ps = pg.prepareStatement(upsert)) {
                int updated = ps.executeUpdate();
                System.out.println("[BackfillPostViewCount] updated posts rows=" + updated);
            }
        }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

