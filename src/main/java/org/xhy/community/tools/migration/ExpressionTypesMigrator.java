package org.xhy.community.tools.migration;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 旧库 MySQL expression_types -> 新库 PostgreSQL expression_types
 * 动态列兼容：通过 ResultSetMetaData 判断可用列，尽量映射 code/name/image_url/sort/is_active/时间。
 */
public class ExpressionTypesMigrator {

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

        System.out.println("[ExpressionTypesMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[ExpressionTypesMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);
            Timestamp now = Timestamp.from(Instant.now());
            // 加载 fileKey -> resourceId 映射（用于将 image_path 转成新资源URL）
            Map<String,String> fk2id = loadFileKeyMap(pg);

            String select = "SELECT * FROM expression_types ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);
                try (ResultSet rs = sel.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    Set<String> cols = new HashSet<>();
                    for (int i=1;i<=md.getColumnCount();i++) cols.add(md.getColumnLabel(i).toLowerCase());

                    String insert = "INSERT INTO expression_types (id, code, name, image_url, sort_order, is_active, create_time, update_time, deleted_at) " +
                            "VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                    try (PreparedStatement ins = pg.prepareStatement(insert)) {
                        int batch=0, batchSize=500; long inserted=0;
                        while (rs.next()) {
                            String id = val(rs, cols, "id");
                            if (id == null) continue;
                            String code = firstNonNull(rs, cols, "code","expression_code","emoji_code");
                            if (code == null || code.isBlank()) continue;
                            String name = firstNonNull(rs, cols, "name","title","label");
                            if (name == null || name.isBlank()) name = code;
                            // 旧库字段为 image_path，这里统一转换为【资源ID】存储
                            String imageRaw = firstNonNull(rs, cols, "image_url","image_path","image","url","img_url","icon");
                            String imageUrl = mapToResourceId(imageRaw, fk2id);
                            if (imageUrl == null || imageUrl.isBlank()) imageUrl = ":"+code+":"; // 兜底（占位，后续可再次修复）
                            Integer sort = toInt(firstNonNull(rs, cols, "sort_order","sort"));
                            Boolean active = toBool(firstNonNull(rs, cols, "is_active","state","enabled"));
                            Timestamp createTime = toTs(rs, cols, "create_time","created_at");
                            Timestamp updateTime = toTs(rs, cols, "update_time","updated_at");
                            Timestamp deletedAt = toTs(rs, cols, "deleted_at");
                            if (createTime == null) createTime = now; if (updateTime == null) updateTime = createTime;
                            if (active == null) active = Boolean.TRUE;
                            if (sort == null) sort = 0;

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, code);
                            ins.setString(i++, name);
                            ins.setString(i++, imageUrl);
                            ins.setInt(i++, sort);
                            ins.setBoolean(i++, active);
                            ins.setTimestamp(i++, createTime);
                            ins.setTimestamp(i++, updateTime);
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            ins.addBatch();
                            if (++batch>=batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                        if (batch>0) inserted += flush(ins, pg);
                        System.out.println("[ExpressionTypesMigrator] inserted="+inserted);
                    }
                }
            }
        }
    }

    private static String val(ResultSet rs, Set<String> cols, String name) throws SQLException {
        return cols.contains(name) ? String.valueOf(rs.getObject(name)) : null;
    }
    private static String firstNonNull(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String n : names) if (cols.contains(n)) { Object o = rs.getObject(n); if (o != null) return String.valueOf(o); }
        return null;
    }
    private static Integer toInt(String s) { try { return s==null?null:Integer.parseInt(s); } catch (Exception e) { return null; } }
    private static Boolean toBool(String s) {
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        if (t.equals("1") || t.equals("true") || t.equals("t")) return true;
        if (t.equals("0") || t.equals("false") || t.equals("f")) return false;
        return null;
    }
    private static Timestamp toTs(ResultSet rs, Set<String> cols, String... names) throws SQLException {
        for (String n : names) if (cols.contains(n)) { Timestamp ts = rs.getTimestamp(n); if (ts != null) return ts; }
        return null;
    }

    private static long flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch(); pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    // ============ 资源映射与URL转换 ============
    private static final Pattern OLD_URL = Pattern.compile("/api/community/file/singUrl\\?fileKey=([^)\\s\\\"'&]+)");
    private static final Pattern NEW_URL = Pattern.compile("/api/public/resource/([^/]+)/access");
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
    private static String mapToResourceId(String v, Map<String,String> fk2id) {
        if (v == null || v.isBlank()) return v;
        // 已是新URL：提取 id
        Matcher nm = NEW_URL.matcher(v);
        if (nm.find()) return nm.group(1);
        // 旧URL：fileKey 参数
        Matcher m = OLD_URL.matcher(v);
        if (m.find()) {
            String key = urlDecode(m.group(1));
            String id = fk2id.get(key);
            if (id != null) return id;
        }
        // 裸 fileKey
        if (v.matches("\\d+/[\\w\\-\\.]+")) {
            String id = fk2id.get(v);
            if (id != null) return id;
        }
        // 可能已经是资源ID（数字串/UUID），则原样返回
        if (v.matches("[0-9a-fA-F\\-]{1,36}")) return v;
        // 残留：旧前缀 + 新URL
        if (v.contains("/api/community/file/singUrl?")) {
            String after = v.substring(v.indexOf('?') + 1);
            Matcher nm2 = NEW_URL.matcher(after);
            if (nm2.find()) return nm2.group(1);
        }
        return v; // 无法解析时保留原值，方便后续人工处理
    }
    private static String urlDecode(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; } }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
