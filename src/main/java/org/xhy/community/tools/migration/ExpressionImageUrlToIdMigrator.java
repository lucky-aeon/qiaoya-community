package org.xhy.community.tools.migration;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 expression_types.image_url 从 旧URL/fileKey/新URL 统一修正为【资源ID】存储。
 */
public class ExpressionImageUrlToIdMigrator {

    private static final Pattern OLD_URL = Pattern.compile("/api/community/file/singUrl\\?fileKey=([^)\\s\\\"'&]+)");
    private static final Pattern NEW_URL = Pattern.compile("/api/public/resource/([^/]+)/access");

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");

        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[ExpressionImageUrlToIdMigrator] Postgres -> " + pgUrl);

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);
            Map<String,String> fk2id = loadFileKeyMap(pg);

            String select = "SELECT id, image_url FROM expression_types ORDER BY id";
            String update = "UPDATE expression_types SET image_url=?, update_time=now() WHERE id=?";
            try (PreparedStatement sel = pg.prepareStatement(select);
                 PreparedStatement upd = pg.prepareStatement(update)) {
                int updated=0, batch=0, batchSize=500;
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String v = rs.getString(2);
                        String rid = mapToResourceId(v, fk2id);
                        if (!safeEq(v, rid)) {
                            upd.setString(1, rid);
                            upd.setString(2, id);
                            upd.addBatch();
                            if (++batch>=batchSize) { upd.executeBatch(); batch=0; }
                            updated++;
                        }
                    }
                }
                if (batch>0) upd.executeBatch();
                pg.commit();
                System.out.println("[ExpressionImageUrlToIdMigrator] updated="+updated);
            }
        }
    }

    private static boolean safeEq(String a, String b) { return (a==null?"":a).equals(b==null?"":b); }
    private static String urlDecode(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; } }

    private static Map<String,String> loadFileKeyMap(Connection pg) throws SQLException {
        Map<String,String> map = new HashMap<>();
        try (PreparedStatement ps = pg.prepareStatement("SELECT file_key, id FROM resources")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String k = rs.getString(1); String v = rs.getString(2);
                    if (k!=null && !k.isBlank()) map.put(k, v);
                }
            }
        }
        return map;
    }

    private static String mapToResourceId(String v, Map<String,String> fk2id) {
        if (v == null || v.isBlank()) return v;
        Matcher nm = NEW_URL.matcher(v);
        if (nm.find()) return nm.group(1);
        Matcher m = OLD_URL.matcher(v);
        if (m.find()) {
            String key = urlDecode(m.group(1));
            String id = fk2id.get(key);
            if (id != null) return id;
        }
        if (v.matches("\\d+/[\\w\\-\\.]+")) {
            String id = fk2id.get(v);
            if (id != null) return id;
        }
        if (v.matches("[0-9a-fA-F\\-]{1,36}")) return v;
        if (v.contains("/api/community/file/singUrl?")) {
            String after = v.substring(v.indexOf('?') + 1);
            Matcher nm2 = NEW_URL.matcher(after);
            if (nm2.find()) return nm2.group(1);
        }
        return v;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

