package org.xhy.community.tools.migration;

import org.xhy.community.infrastructure.markdown.MarkdownUrlRewriter;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 旧库 MySQL courses -> 新库 PostgreSQL courses
 * 字段映射：
 * - id -> id (string)
 * - title -> title
 * - desc -> description
 * - technology -> tech_stack(json 数组，按逗号/空白切分)
 * - url -> project_url
 * - cover -> cover_image（需要映射为资源ID：fileKey/url -> resources.id）
 * - money(int, 元) -> price(numeric)
 * - score(int) -> rating(numeric)
 * - state -> status(PENDING/IN_PROGRESS/COMPLETED) [口径：1=PENDING, 2=IN_PROGRESS, 其他=COMPLETED]
 * - user_id -> author_id
 * - resources(json) -> resources(json) 原样迁移
 * - demo_url 如为旧 fileKey 链接则转换为新 URL（否则保持）
 * - created_at/updated_at/deleted_at 对齐 create_time/update_time/deleted_at
 */
public class CoursesMigrator {

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

        System.out.println("[CoursesMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[CoursesMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            Map<String,String> fk2id = loadFileKeyMap(pg);
            MarkdownUrlRewriter rewriter = new MarkdownUrlRewriter();
            Timestamp now = Timestamp.from(Instant.now());

            String select = "SELECT id, title, `desc` AS description, technology, url, cover, money, state, score, user_id, resources, demo_url, created_at, updated_at, deleted_at FROM courses ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);

                String insert = "INSERT INTO courses (id, title, description, tech_stack, project_url, tags, rating, status, author_id, total_reading_time, create_time, update_time, price, original_price, cover_image, demo_url, resources, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch=0, batchSize=500; long inserted=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt("id"));
                            String title = rs.getString("title");
                            String description = rs.getString("description");
                            String technology = rs.getString("technology");
                            String projectUrl = rs.getString("url");
                            String cover = rs.getString("cover");
                            String money = rs.getString("money");
                            Integer state = objToInt(rs.getObject("state"));
                            String score = rs.getString("score");
                            String authorId = String.valueOf(rs.getInt("user_id"));
                            String resources = rs.getString("resources");
                            String demoUrl = rs.getString("demo_url");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            Timestamp deletedAt = rs.getTimestamp("deleted_at");
                            if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = createdAt;

                            String techStackJson = toArrayJson(splitTech(technology));
                            String tagsJson = null; // 无来源，留空
                            BigDecimal rating = score == null ? null : new BigDecimal(score);
                            String status = mapStatus(state);
                            int totalReadingTime = 0;
                            BigDecimal price = money == null ? null : new BigDecimal(money);
                            BigDecimal originalPrice = null;

                            String coverId = mapCoverToResourceId(cover, fk2id);
                            String demoUrlNew = mapUrlToNew(demoUrl, fk2id);

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, title);
                            ins.setString(i++, description);
                            if (techStackJson != null) ins.setObject(i++, techStackJson, Types.OTHER); else ins.setNull(i++, Types.OTHER);
                            ins.setString(i++, projectUrl);
                            if (tagsJson != null) ins.setObject(i++, tagsJson, Types.OTHER); else ins.setNull(i++, Types.OTHER);
                            if (rating != null) ins.setBigDecimal(i++, rating); else ins.setNull(i++, Types.NUMERIC);
                            ins.setString(i++, status);
                            ins.setString(i++, authorId);
                            ins.setInt(i++, totalReadingTime);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            if (price != null) ins.setBigDecimal(i++, price); else ins.setNull(i++, Types.NUMERIC);
                            if (originalPrice != null) ins.setBigDecimal(i++, originalPrice); else ins.setNull(i++, Types.NUMERIC);
                            ins.setString(i++, coverId);
                            ins.setString(i++, demoUrlNew);
                            if (resources != null) ins.setObject(i++, resources, Types.OTHER); else ins.setNull(i++, Types.OTHER);
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                    }
                    if (batch>0) inserted += flush(ins, pg);
                    System.out.println("[CoursesMigrator] inserted="+inserted);
                }
            }
        }
    }

    private static String mapCoverToResourceId(String cover, Map<String,String> fk2id) {
        if (cover == null || cover.isBlank()) return null;
        String prefix = "/api/public/resource/";
        int idx = cover.indexOf(prefix);
        if (idx >= 0) {
            int start = idx + prefix.length();
            int end = cover.indexOf('/', start);
            if (end > start) return cover.substring(start, end);
        }
        if (cover.contains("singUrl?fileKey=")) {
            String fk = cover.substring(cover.indexOf("singUrl?fileKey=") + "singUrl?fileKey=".length());
            int cut = fk.indexOf('&'); if (cut > 0) fk = fk.substring(0, cut);
            fk = urlDecode(fk);
            String id = fk2id.get(fk);
            if (id != null) return id;
        }
        if (cover.matches("\\d+/[\\w\\-\\.]+")) {
            String id = fk2id.get(cover);
            if (id != null) return id;
        }
        // 可能已经是资源ID（UUID 或数字串），直接返回
        return cover;
    }

    private static String mapUrlToNew(String url, Map<String,String> fk2id) {
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

    private static String urlDecode(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; } }

    private static String mapStatus(Integer state) {
        // 新口径：1=IN_PROGRESS, 2=COMPLETED，其余=PENDING
        if (state != null) {
            if (state == 2) return "COMPLETED";
            if (state == 1) return "IN_PROGRESS";
        }
        return "PENDING";
    }

    private static List<String> splitTech(String tech) {
        if (tech == null || tech.isBlank()) return Collections.emptyList();
        String norm = tech.replace('；', ';').replace('，', ',');
        String[] parts = norm.split("[;,\\s]+");
        List<String> list = new ArrayList<>();
        for (String p : parts) { if (!p.isBlank()) list.add(p.trim()); }
        return list;
    }

    private static String toArrayJson(List<String> items) {
        if (items == null || items.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i=0;i<items.size();i++) {
            if (i>0) sb.append(',');
            sb.append('"').append(items.get(i).replace("\\", "\\\\").replace("\"","\\\"")).append('"');
        }
        return sb.append(']').toString();
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
                    String k = rs.getString(1); String v = rs.getString(2);
                    if (k!=null && !k.isBlank()) map.put(k,v);
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

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
