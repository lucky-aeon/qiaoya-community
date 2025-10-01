package org.xhy.community.tools.migration;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;

/**
 * 紧急恢复：从新库 PostgreSQL 的 courses/resources 反填旧库 MySQL 的 courses 表。
 * 适用于误删旧库 courses 数据且新库已有较完整课程数据的场景。
 *
 * 字段映射：
 * - id(varchar) → id(int)
 * - title → title
 * - description → `desc`
 * - tech_stack(json) → technology（逗号分隔）
 * - project_url → url
 * - cover_image(id/可能为空) → cover(file_key)（通过 PG resources 映射）
 * - price(numeric, 元) → money(int)
 * - status(PENDING/IN_PROGRESS/COMPLETED) → state(int)：IN_PROGRESS→1，COMPLETED→2，其余→1
 * - rating(numeric) → score(int)
 * - author_id(varchar) → user_id(int)
 * - resources(json) → resources(json)
 * - demo_url：保持为新 URL（或外链）
 * - created_at/updated_at/deleted_at：按 PG create_time/update_time/deleted_at
 */
public class RestoreOldCoursesFromPostgres {

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
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=8000&socketTimeout=30000&zeroDateTimeBehavior=convertToNull",
                mysqlHost, mysqlPort, mysqlDb);
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[RestoreOldCoursesFromPostgres] MySQL -> " + mysqlUrl);
        System.out.println("[RestoreOldCoursesFromPostgres] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            mysql.setAutoCommit(false);

            String select = "SELECT id, title, description, tech_stack::text, project_url, price, status, rating, author_id, resources::text, demo_url, create_time, update_time, deleted_at, cover_image FROM courses ORDER BY id";
            try (PreparedStatement sel = pg.prepareStatement(select)) {
                String coverKeySql = "SELECT file_key FROM resources WHERE id=?";
                try (PreparedStatement coverKeyStmt = pg.prepareStatement(coverKeySql)) {

                    String insert = "INSERT INTO courses (id, title, `desc`, technology, url, cover, money, state, score, user_id, resources, demo_url, created_at, updated_at, deleted_at) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                            "title=VALUES(title), `desc`=VALUES(`desc`), technology=VALUES(technology), url=VALUES(url), cover=VALUES(cover), money=VALUES(money), state=VALUES(state), score=VALUES(score), user_id=VALUES(user_id), resources=VALUES(resources), demo_url=VALUES(demo_url), created_at=VALUES(created_at), updated_at=VALUES(updated_at), deleted_at=VALUES(deleted_at)";
                    try (PreparedStatement ins = mysql.prepareStatement(insert)) {
                        int batch = 0, batchSize = 300; long restored = 0;
                        try (ResultSet rs = sel.executeQuery()) {
                            while (rs.next()) {
                                String pgId = rs.getString(1);
                                Integer id = parseInt(pgId);
                                if (id == null) continue; // 跳过非整型ID

                                String title = rs.getString(2);
                                String description = rs.getString(3);
                                String techStack = rs.getString(4); // JSON 文本
                                String technology = jsonArrayToCsv(techStack);
                                String projectUrl = rs.getString(5);
                                BigDecimal price = (BigDecimal) rs.getObject(6);
                                String status = rs.getString(7);
                                BigDecimal rating = (BigDecimal) rs.getObject(8);
                                String authorId = rs.getString(9);
                                String resources = rs.getString(10);
                                String demoUrl = rs.getString(11);
                                Timestamp createdAt = rs.getTimestamp(12);
                                Timestamp updatedAt = rs.getTimestamp(13);
                                Timestamp deletedAt = rs.getTimestamp(14);
                                String coverImageId = rs.getString(15);

                                String coverKey = null;
                                if (coverImageId != null && !coverImageId.isBlank()) {
                                    coverKeyStmt.setString(1, coverImageId);
                                    try (ResultSet kr = coverKeyStmt.executeQuery()) {
                                        if (kr.next()) coverKey = kr.getString(1);
                                    }
                                }

                                int money = price == null ? 0 : price.intValue();
                                int state = mapStatus(status);
                                int score = rating == null ? 0 : rating.intValue();
                                Integer userId = parseInt(authorId);

                                int i=1;
                                ins.setInt(i++, id);
                                ins.setString(i++, title);
                                ins.setString(i++, description);
                                ins.setString(i++, technology);
                                ins.setString(i++, projectUrl);
                                ins.setString(i++, coverKey);
                                ins.setInt(i++, money);
                                ins.setInt(i++, state);
                                ins.setInt(i++, score);
                                ins.setInt(i++, userId == null ? 0 : userId);
                                ins.setString(i++, resources);
                                ins.setString(i++, demoUrl);
                                ins.setTimestamp(i++, createdAt);
                                ins.setTimestamp(i++, updatedAt);
                                if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                                ins.addBatch();
                                if (++batch >= batchSize) { ins.executeBatch(); mysql.commit(); batch = 0; }
                                restored++;
                            }
                        }
                        if (batch > 0) { ins.executeBatch(); mysql.commit(); }
                        System.out.println("[RestoreOldCoursesFromPostgres] restored=" + restored);
                    }
                }
            }
        }
    }

    private static Integer parseInt(String s) { try { return s == null ? null : Integer.parseInt(s); } catch (Exception e) { return null; } }

    private static int mapStatus(String status) {
        if (status == null) return 1; // 默认更新中
        switch (status) {
            case "COMPLETED": return 2;
            case "IN_PROGRESS": return 1;
            default: return 1; // PENDING 也按 1 处理
        }
    }

    private static String jsonArrayToCsv(String json) {
        if (json == null || json.isBlank()) return null;
        String t = json.trim();
        if (t.startsWith("[") && t.endsWith("]")) {
            t = t.substring(1, t.length()-1).trim();
            if (t.isEmpty()) return null;
            // 简单处理：移除引号并按逗号拼接
            String[] parts = t.split(",");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                String v = p.trim();
                if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length()-1);
                v = v.replace("\\\"","\"").replace("\\\\","\\");
                if (!v.isBlank()) {
                    if (sb.length() > 0) sb.append(',');
                    sb.append(v);
                }
            }
            String out = sb.toString();
            return out.isEmpty() ? null : out;
        }
        return json; // 不是数组时原样
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
