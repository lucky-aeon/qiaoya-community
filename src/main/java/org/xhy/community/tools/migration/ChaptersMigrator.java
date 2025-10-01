package org.xhy.community.tools.migration;

import org.xhy.community.infrastructure.markdown.MarkdownUrlRewriter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 旧库 MySQL courses_sections -> 新库 PostgreSQL chapters
 * 字段映射：
 * - id -> id (string)
 * - title -> title
 * - content -> content（使用 AST 重写旧 fileKey 链接为新资源访问URL）
 * - course_id -> course_id (string)
 * - user_id -> author_id (string)
 * - sort -> sort_order
 * - reading_time -> reading_time
 * - created_at/updated_at/deleted_at 对齐
 */
public class ChaptersMigrator {

    private static final Pattern OLD_URL = Pattern.compile("/api/community/file/singUrl\\?fileKey=([^)\\s\\\"'&]+)");

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

        System.out.println("[ChaptersMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[ChaptersMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            Map<String,String> fk2id = loadFileKeyMap(pg);
            MarkdownUrlRewriter rewriter = new MarkdownUrlRewriter();
            Timestamp now = Timestamp.from(Instant.now());

            // 旧表 courses_sections 无 updated_at 字段
            String select = "SELECT id, title, content, course_id, user_id, sort, reading_time, created_at, deleted_at FROM courses_sections ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);
                String insert = "INSERT INTO chapters (id, title, content, course_id, author_id, sort_order, reading_time, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch=0, batchSize=500; long inserted=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt("id"));
                            String title = rs.getString("title");
                            String content = rs.getString("content");
                            String courseId = String.valueOf(rs.getInt("course_id"));
                            String authorId = String.valueOf(rs.getInt("user_id"));
                            Integer sort = objToInt(rs.getObject("sort"));
                            Integer readingTime = objToInt(rs.getObject("reading_time"));
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp deletedAt = rs.getTimestamp("deleted_at");
                            if (createdAt == null) createdAt = now;
                            Timestamp updatedAt = createdAt;

                            String newContent = rewriteContent(rewriter, content, fk2id);

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, title);
                            ins.setString(i++, newContent == null ? "" : newContent);
                            ins.setString(i++, courseId);
                            ins.setString(i++, authorId);
                            ins.setInt(i++, sort == null ? 0 : sort);
                            ins.setInt(i++, readingTime == null ? 0 : readingTime);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                    }
                    if (batch>0) inserted += flush(ins, pg);
                    System.out.println("[ChaptersMigrator] inserted="+inserted);
                }
            }
        }
    }

    private static String rewriteContent(MarkdownUrlRewriter rewriter, String content, Map<String,String> fk2id) {
        if (content == null || content.isBlank()) return content;
        String rendered = rewriter.rewrite(content, url -> mapOldToNew(url, fk2id));
        return replacePlainOldUrl(rendered, fk2id);
    }

    private static String mapOldToNew(String url, Map<String,String> fk2id) {
        if (url == null || url.isBlank()) return url;
        if (url.contains("/api/community/file/singUrl?")) {
            String after = url.substring(url.indexOf('?') + 1);
            if (after.startsWith("/api/public/resource/")) return after;
            if (after.matches("\\d+/[\\w\\-\\.]+")) {
                String id = fk2id.get(after);
                if (id != null) return "/api/public/resource/"+id+"/access";
            }
        }
        Matcher m = OLD_URL.matcher(url);
        if (m.find()) {
            String enc = m.group(1);
            String key = urlDecode(enc);
            String id = fk2id.get(key);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        if (url.matches("\\d+/[\\w\\-\\.]+")) {
            String id = fk2id.get(url);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        return url;
    }

    private static String replacePlainOldUrl(String text, Map<String,String> fk2id) {
        if (text == null || text.isBlank()) return text;
        StringBuffer sb = new StringBuffer();
        Matcher m = OLD_URL.matcher(text);
        while (m.find()) {
            String enc = m.group(1);
            String key = urlDecode(enc);
            String id = fk2id.get(key);
            if (id != null) m.appendReplacement(sb, Matcher.quoteReplacement("/api/public/resource/"+id+"/access"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static int flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch(); pg.commit();
        int ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static Map<String,String> loadFileKeyMap(Connection pg) throws SQLException {
        Map<String,String> map = new HashMap<>();
        try (PreparedStatement ps = pg.prepareStatement("SELECT file_key, id FROM resources")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String k = rs.getString(1);
                    String v = rs.getString(2);
                    if (k != null && !k.isBlank()) map.put(k, v);
                }
            }
        }
        return map;
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

    private static String urlDecode(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; } }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
