package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 旧库 MySQL qa_adoptions -> 新库 PostgreSQL post_accepted_comments
 * 额外：将 posts.resolve_status 置为 SOLVED，solved_at=最早被采纳时间（若已存在则不覆盖）
 */
public class PostAcceptedCommentsMigrator {

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

        System.out.println("[PostAcceptedCommentsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[PostAcceptedCommentsMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);
            Timestamp now = Timestamp.from(Instant.now());

            // 1) 插入 post_accepted_comments
            String select = "SELECT id, article_id, comment_id, created_at FROM qa_adoptions ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);

                String insert = "INSERT INTO post_accepted_comments (id, post_id, comment_id, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,NULL) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch=0, batchSize=500; long inserted=0;
                    Map<String, Timestamp> postSolvedAt = new HashMap<>();
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt(1));
                            String postId = String.valueOf(rs.getInt(2));
                            String commentId = String.valueOf(rs.getInt(3));
                            Timestamp createdAt = rs.getTimestamp(4);
                            if (createdAt == null) createdAt = now;

                            // insert
                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, postId);
                            ins.setString(i++, commentId);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, createdAt);
                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }

                            // track earliest solvedAt
                            Timestamp prev = postSolvedAt.get(postId);
                            if (prev == null || createdAt.before(prev)) postSolvedAt.put(postId, createdAt);
                        }
                    }
                    if (batch>0) inserted += flush(ins, pg);
                    System.out.println("[PostAcceptedCommentsMigrator] inserted="+inserted);

                    // 2) 更新 posts.resolve_status/solved_at（只在未设定时或非 SOLVED 时更新）
                    String updatePost = "UPDATE posts SET resolve_status='SOLVED', solved_at=COALESCE(solved_at, ?), update_time=now() " +
                            "WHERE id=? AND (resolve_status IS NULL OR resolve_status <> 'SOLVED')";
                    try (PreparedStatement upd = pg.prepareStatement(updatePost)) {
                        int ub=0, ubSize=500, updated=0;
                        for (Map.Entry<String, Timestamp> e : postSolvedAt.entrySet()) {
                            upd.setTimestamp(1, e.getValue());
                            upd.setString(2, e.getKey());
                            upd.addBatch();
                            if (++ub >= ubSize) { updated += sum(upd.executeBatch()); ub=0; }
                        }
                        if (ub>0) updated += sum(upd.executeBatch());
                        pg.commit();
                        System.out.println("[PostAcceptedCommentsMigrator] posts updated="+updated);
                    }
                }
            }
        }
    }

    private static long flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch(); pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static int sum(int[] arr) { int s=0; for (int v:arr) if (v>0 || v==Statement.SUCCESS_NO_INFO) s++; return s; }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
