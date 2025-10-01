package org.xhy.community.tools.migration;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 expression_types.image_url 中的旧 fileKey/旧URL 统一替换为新资源访问 URL：/api/public/resource/{id}/access
 * 依赖 resources(file_key -> id) 映射。
 */
public class ExpressionImageUrlRewriteMigrator {

    private static final Pattern OLD_URL = Pattern.compile("/api/community/file/singUrl\\?fileKey=([^)\\s\\\"'&]+)");

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");

        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[ExpressionImageUrlRewriteMigrator] Postgres -> " + pgUrl);

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);

            Map<String,String> fk2id = loadFileKeyMap(pg);
            System.out.println("[ExpressionImageUrlRewriteMigrator] mappings=" + fk2id.size());

            String select = "SELECT id, image_url FROM expression_types ORDER BY id";
            String update = "UPDATE expression_types SET image_url=?, update_time=now() WHERE id=?";

            try (PreparedStatement sel = pg.prepareStatement(select);
                 PreparedStatement upd = pg.prepareStatement(update)) {
                int batch=0, batchSize=500, changed=0;
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String url = rs.getString(2);
                        String newUrl = mapToNewUrl(url, fk2id);
                        if (!safeEq(url, newUrl)) {
                            upd.setString(1, newUrl);
                            upd.setString(2, id);
                            upd.addBatch();
                            if (++batch>=batchSize) { upd.executeBatch(); batch=0; }
                            changed++;
                        }
                    }
                }
                if (batch>0) upd.executeBatch();
                pg.commit();
                System.out.println("[ExpressionImageUrlRewriteMigrator] updated="+changed);
            }
        }
    }

    private static String mapToNewUrl(String url, Map<String,String> fk2id) {
        if (url == null || url.isBlank()) return url;
        // 已是新URL
        if (url.startsWith("/api/public/resource/")) return url;
        // 旧URL + fileKey 参数
        Matcher m = OLD_URL.matcher(url);
        if (m.find()) {
            String enc = m.group(1);
            String key = urlDecode(enc);
            String id = fk2id.get(key);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        // 裸 fileKey
        if (url.matches("\\d+/[\\w\\-\\.]+")) {
            String id = fk2id.get(url);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        // 残留：/api/community/file/singUrl? + 已是新URL
        if (url.contains("/api/community/file/singUrl?")) {
            String after = url.substring(url.indexOf('?') + 1);
            if (after.startsWith("/api/public/resource/")) return after;
        }
        return url;
    }

    private static boolean safeEq(String a, String b) { return (a==null?"":a).equals(b==null?"":b); }
    private static String urlDecode(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; } }

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

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

