package org.xhy.community.tools.migration;

import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;

/**
 * 迁移 AI 日报：ai_news(MySQL) -> ai_daily_items(PostgreSQL)
 * 映射：
 * - id -> id (字符串)
 * - source -> 固定 "AIBASE"
 * - title/summary/content -> 对齐（content 为空时置空串）
 * - url -> source_url（必填，无则跳过）
 * - url_hash -> 旧表 hash；为空则 md5(url)
 * - published_at -> publish_date 优先；缺失回退 created_at；再回退 now
 * - fetched_at -> created_at；缺失回退 now
 * - status -> 旧 status!=0 则 PUBLISHED，否则 HIDDEN
 * - metadata(jsonb) -> {"source_name":"...","category":"...","tags":"..."}
 * - create_time/update_time -> fetched_at / updated_at(缺失回退 fetched_at)
 * - deleted_at -> 对齐
 *
 * 幂等：ON CONFLICT(id) DO NOTHING
 * 支持：DRY_RUN 与 BATCH_SIZE（默认1000）
 */
public class AiDailyItemsMigrator {

    public static void main(String[] args) throws Exception {
        boolean dryRun = Boolean.parseBoolean(env("DRY_RUN", "false"));
        int batchSize = parseInt(env("BATCH_SIZE", "1000"), 1000);

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

        System.out.println("[AiDailyItemsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[AiDailyItemsMigrator] Postgres -> " + pgUrl + " dryRun=" + dryRun);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String select = "SELECT id, title, content, summary, source_url, source_name, publish_date, category, tags, status, hash, created_at, updated_at, deleted_at FROM ai_news ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);

                String insert = "INSERT INTO ai_daily_items (id, source, title, summary, content, url, source_item_id, published_at, fetched_at, url_hash, status, metadata, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    long total=0, inserted=0, skippedNoUrl=0; int batch=0;
                    Timestamp now = Timestamp.from(Instant.now());
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            total++;
                            String id = String.valueOf(rs.getInt("id"));
                            String title = rs.getString("title");
                            String content = rs.getString("content");
                            String summary = rs.getString("summary");
                            String url = rs.getString("source_url");
                            String sourceName = rs.getString("source_name");
                            Timestamp publishDate = rs.getTimestamp("publish_date");
                            String category = rs.getString("category");
                            String tags = rs.getString("tags");
                            Integer status = objToInt(rs.getObject("status"));
                            String urlHash = rs.getString("hash");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            Timestamp deletedAt = rs.getTimestamp("deleted_at");

                            if (url == null || url.isBlank()) { skippedNoUrl++; continue; }
                            if (content == null) content = ""; // 非空约束

                            Timestamp publishedAt = publishDate != null ? publishDate : (createdAt != null ? createdAt : now);
                            Timestamp fetchedAt = createdAt != null ? createdAt : now;
                            if (updatedAt == null) updatedAt = fetchedAt;
                            if (urlHash == null || urlHash.isBlank()) urlHash = md5(url);

                            String newStatus = (status != null && status != 0) ? "PUBLISHED" : "HIDDEN";
                            String metadata = buildMetadata(sourceName, category, tags);

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, "AIBASE"); // source
                            ins.setString(i++, title);
                            if (summary == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, summary);
                            ins.setString(i++, content);
                            ins.setString(i++, url);
                            ins.setNull(i++, Types.BIGINT); // source_item_id 无
                            ins.setTimestamp(i++, publishedAt);
                            ins.setTimestamp(i++, fetchedAt);
                            ins.setString(i++, urlHash);
                            ins.setString(i++, newStatus);
                            if (metadata == null) ins.setNull(i++, Types.OTHER); else ins.setObject(i++, metadata, Types.OTHER);
                            ins.setTimestamp(i++, fetchedAt); // create_time
                            ins.setTimestamp(i++, updatedAt);  // update_time
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            if (!dryRun) ins.addBatch();
                            batch++;
                            if (!dryRun && batch >= batchSize) {
                                ins.executeBatch();
                                pg.commit();
                                batch = 0;
                            }
                            inserted++;
                        }
                    }

                    if (!dryRun && batch > 0) { ins.executeBatch(); pg.commit(); }
                    System.out.printf("[AiDailyItemsMigrator] total=%d, inserted=%d, skippedNoUrl=%d\n", total, inserted, skippedNoUrl);
                }
            }
        }
    }

    private static Integer objToInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Short) return ((Short) o).intValue();
        if (o instanceof Byte) return ((Byte) o).intValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0;
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static String buildMetadata(String sourceName, String category, String tags) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        if (notBlank(sourceName)) {
            if (!first) sb.append(','); first=false;
            sb.append("\"source_name\":\"").append(escape(sourceName)).append("\"");
        }
        if (notBlank(category)) {
            if (!first) sb.append(','); first=false;
            sb.append("\"category\":\"").append(escape(category)).append("\"");
        }
        if (notBlank(tags)) {
            if (!first) sb.append(','); first=false;
            sb.append("\"tags\":\"").append(escape(tags)).append("\"");
        }
        sb.append('}');
        String json = sb.toString();
        // 若全部为空，返回 null
        return json.equals("{}") ? null : json;
    }

    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static String escape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder(bytes.length*2);
            for (byte b: bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return Long.toHexString(Double.doubleToLongBits(Math.random())); }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
