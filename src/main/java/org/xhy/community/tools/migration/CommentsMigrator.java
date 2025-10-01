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
 * 旧库 MySQL comments -> 新库 PostgreSQL comments
 * 字段映射：
 * - id -> id (string)
 * - parent_id -> parent_comment_id (0 或与自己相等时置 null)
 * - root_id -> root_comment_id (0 置 null)
 * - content -> content（同时重写旧 fileKey 链接为新资源访问URL）
 * - from_user_id -> comment_user_id (string)
 * - to_user_id -> reply_user_id (string, 可空)
 * - business_id -> business_id (string)
 * - business_type 固定为 'POST'（如需要支持课程/章节，可再扩展）
 * - created_at/updated_at/deleted_at 对齐为 create_time/update_time/deleted_at
 */
public class CommentsMigrator {

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

        System.out.println("[CommentsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[CommentsMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            Map<String,String> fk2id = loadFileKeyMap(pg);
            MarkdownUrlRewriter rewriter = new MarkdownUrlRewriter();
            Timestamp now = Timestamp.from(Instant.now());

            String select = "SELECT id, parent_id, root_id, content, from_user_id, to_user_id, business_id, tenant_id, created_at, updated_at, deleted_at FROM comments ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String insert = "INSERT INTO comments (id, parent_comment_id, root_comment_id, content, comment_user_id, reply_user_id, business_id, business_type, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch = 0, batchSize = 1000; long inserted=0, skippedUnsupported=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt("id"));
                            int parent = safeInt(rs.getObject("parent_id"));
                            int root = safeInt(rs.getObject("root_id"));
                            String content = rs.getString("content");
                            int fromUser = safeInt(rs.getObject("from_user_id"));
                            Integer toUser = objToInt(rs.getObject("to_user_id"));
                            int bizId = safeInt(rs.getObject("business_id"));
                            Integer tenantId = objToInt(rs.getObject("tenant_id"));
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            Timestamp deletedAt = rs.getTimestamp("deleted_at");
                            if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = createdAt;

                            String parentId = (parent <= 0 || parent == Integer.parseInt(id)) ? null : String.valueOf(parent);
                            String rootId = (root <= 0) ? null : String.valueOf(root);
                            String commentUserId = String.valueOf(fromUser);
                            String replyUserId = toUser == null ? null : String.valueOf(toUser);
                            String businessId = String.valueOf(bizId);

                            String newContent = rewriteContent(rewriter, content, fk2id);

                            String businessType = mapBusinessType(tenantId);
                            if (businessType == null) { skippedUnsupported++; continue; }

                            int i=1;
                            ins.setString(i++, id);
                            if (parentId != null) ins.setString(i++, parentId); else ins.setNull(i++, Types.VARCHAR);
                            if (rootId != null) ins.setString(i++, rootId); else ins.setNull(i++, Types.VARCHAR);
                            ins.setString(i++, newContent == null ? "" : newContent);
                            ins.setString(i++, commentUserId);
                            if (replyUserId != null) ins.setString(i++, replyUserId); else ins.setNull(i++, Types.VARCHAR);
                            ins.setString(i++, businessId);
                            ins.setString(i++, businessType);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }
                        }
                    }
                    if (batch > 0) inserted += flush(ins, pg);
                    System.out.println("[CommentsMigrator] inserted=" + inserted + ", skippedUnsupported="+skippedUnsupported);
                }
            }
        }
    }

    private static String rewriteContent(MarkdownUrlRewriter rewriter, String content, Map<String,String> fk2id) {
        if (content == null || content.isBlank()) return content;
        // 使用 AST 改写 Link/Image 的 url
        String rendered = rewriter.rewrite(content, url -> mapOldToNew(url, fk2id));
        // 明文兜底
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

    private static int safeInt(Object o) { Integer v = objToInt(o); return v == null ? 0 : v; }
    private static Integer objToInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Short) return ((Short) o).intValue();
        if (o instanceof Byte) return ((Byte) o).intValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0;
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static long flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch(); pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String mapBusinessType(Integer tenantId) {
        if (tenantId == null) return "POST";
        switch (tenantId) {
            case 0: return "POST";
            case 1: return "CHAPTER";
            case 2: return "COURSE";
            // 3=分享会, 4=AI日报 -> 新系统无对应业务类型，暂跳过
            default: return null;
        }
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

    private static String urlDecode(String s) {
        try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
