package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;

/**
 * 旧库 MySQL update_logs -> 新库 PostgreSQL update_logs
 * 字段映射：
 * - id(bigint)            -> id(varchar)
 * - version               -> version（唯一约束，冲突即跳过）
 * - title                 -> title
 * - description/content   -> description（优先 description，空则用 content）
 * - status                -> status（active/published -> PUBLISHED，其余 -> DRAFT）
 * - publish_date/created_at/updated_at -> create_time/update_time（优先 publish_date）
 * - author_id             -> null
 * - deleted_at            -> null
 */
public class UpdateLogsMigrator {

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

        System.out.println("[UpdateLogsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[UpdateLogsMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);
            Timestamp now = Timestamp.from(Instant.now());

            String select = "SELECT id, title, description, content, version, status, publish_date, created_at, updated_at FROM update_logs ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);

                // 以 version 唯一键做冲突忽略，避免旧库版本重复导致失败
                String insert = "INSERT INTO update_logs (id, version, title, description, author_id, status, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?, ?, NULL) ON CONFLICT (version) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch=0, batchSize=500; long inserted=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getLong(1));
                            String title = rs.getString(2);
                            String description = rs.getString(3);
                            String content = rs.getString(4);
                            String version = rs.getString(5);
                            String statusOld = rs.getString(6);
                            Timestamp publishDate = rs.getTimestamp(7);
                            Timestamp createdAt = rs.getTimestamp(8);
                            Timestamp updatedAt = rs.getTimestamp(9);

                            String descFinal = (description != null && !description.isBlank()) ? description : content;
                            if (descFinal == null) descFinal = "";
                            String status = mapStatus(statusOld);
                            Timestamp createTime = publishDate != null ? publishDate : (createdAt != null ? createdAt : now);
                            Timestamp updateTime = updatedAt != null ? updatedAt : createTime;

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, version);
                            ins.setString(i++, title);
                            ins.setString(i++, descFinal);
                            ins.setNull(i++, Types.VARCHAR); // author_id unknown
                            ins.setString(i++, status);
                            ins.setTimestamp(i++, createTime);
                            ins.setTimestamp(i++, updateTime);

                            ins.addBatch();
                            if (++batch>=batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                    }
                    if (batch>0) inserted += flush(ins, pg);
                    System.out.println("[UpdateLogsMigrator] inserted="+inserted);
                }
            }
        }
    }

    private static String mapStatus(String s) {
        if (s == null) return "PUBLISHED";
        String t = s.trim().toLowerCase();
        if (t.equals("active") || t.equals("published") || t.equals("publish") ) return "PUBLISHED";
        if (t.equals("draft")) return "DRAFT";
        return "PUBLISHED";
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
