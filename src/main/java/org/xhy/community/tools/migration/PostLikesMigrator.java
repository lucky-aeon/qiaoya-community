package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;

/**
 * 旧库 MySQL article_likes -> 新库 PostgreSQL post_likes
 * 映射：id→id，article_id→post_id，user_id→user_id
 */
public class PostLikesMigrator {

    public static void main(String[] args) throws Exception {
        String mysqlHost = env("LEGACY_MYSQL_HOST", "124.220.234.136");
        String mysqlPort = env("LEGACY_MYSQL_PORT", "3306");
        String mysqlDb = env("LEGACY_MYSQL_DB", "community");
        String mysqlUser = env("LEGACY_MYSQL_USER", env("DB_USER", "community"));
        String mysqlPass = env("LEGACY_MYSQL_PASS", env("DB_PASS", null));

        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));

        if (mysqlPass == null || mysqlPass.isEmpty()) throw new IllegalArgumentException("Missing LEGACY_MYSQL_PASS/DB_PASS");
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");

        String mysqlUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&useCursorFetch=true&connectTimeout=8000&socketTimeout=30000&zeroDateTimeBehavior=convertToNull",
                mysqlHost, mysqlPort, mysqlDb);
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[PostLikesMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[PostLikesMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);
            Timestamp now = Timestamp.from(Instant.now());

            String select = "SELECT id, article_id, user_id FROM article_likes ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                // 新库已统一使用 likes 表（target_type 支持 POST/COURSE/CHAPTER/COMMENT）
                String insert = "INSERT INTO likes (id, user_id, target_type, target_id, create_time, update_time) " +
                        "VALUES (?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch=0, batchSize=1000; long inserted=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt(1));
                            String postId = String.valueOf(rs.getInt(2));
                            String userId = String.valueOf(rs.getInt(3));

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, userId);
                            ins.setString(i++, "POST");
                            ins.setString(i++, postId);
                            ins.setTimestamp(i++, now);
                            ins.setTimestamp(i++, now);

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }
                        }
                    }
                    if (batch > 0) inserted += flush(ins, pg);
                    System.out.println("[PostLikesMigrator] inserted="+inserted);
                }
            }
        }
    }

    private static long flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch(); pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
