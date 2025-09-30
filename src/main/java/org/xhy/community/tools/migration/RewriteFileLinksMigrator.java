package org.xhy.community.tools.migration;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 posts.content / posts.cover_image / users.avatar 中的旧 fileKey 链接替换为新资源访问URL：/api/public/resource/{id}/access
 * 依赖 resources 表中 (file_key -> id) 的映射（请先运行 FilesToResourcesMigrator）。
 */
public class RewriteFileLinksMigrator {

    private static final Pattern FILEKEY_PARAM = Pattern.compile("fileKey=([^\\)\\]\\s\"']+)");
    private static final Pattern PLAIN_FILEKEY_IN_PARENS = Pattern.compile("\\((\\d+/[\\w\\-\\.]+)\\)");

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));

        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[RewriteFileLinksMigrator] Postgres → " + pgUrl);

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);

            Map<String,String> fk2id = loadFileKeyMap(pg);
            System.out.println("[RewriteFileLinksMigrator] fileKey mappings: " + fk2id.size());

            rewritePosts(pg, fk2id);
            rewriteUsers(pg, fk2id);

            pg.commit();
        }
    }

    private static Map<String,String> loadFileKeyMap(Connection pg) throws SQLException {
        Map<String,String> map = new HashMap<>();
        try (PreparedStatement ps = pg.prepareStatement("SELECT file_key, id FROM resources")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString(1);
                    String id = rs.getString(2);
                    if (key != null && !key.isBlank()) map.put(key, id);
                }
            }
        }
        return map;
    }

    private static void rewritePosts(Connection pg, Map<String,String> fk2id) throws SQLException {
        String select = "SELECT id, content, cover_image FROM posts ORDER BY id";
        String update = "UPDATE posts SET content=?, cover_image=?, update_time=? WHERE id=?";
        try (PreparedStatement sel = pg.prepareStatement(select);
             PreparedStatement upd = pg.prepareStatement(update)) {
            Timestamp now = Timestamp.from(Instant.now());
            int batch=0, batchSize=500, changed=0;
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    String content = rs.getString(2);
                    String cover = rs.getString(3);
                    String newContent = rewriteText(content, fk2id);
                    String newCover = rewriteSingle(cover, fk2id);
                    if (!eq(content,newContent) || !eq(cover,newCover)) {
                        int i=1;
                        upd.setString(i++, newContent);
                        upd.setString(i++, newCover);
                        upd.setTimestamp(i++, now);
                        upd.setString(i++, id);
                        upd.addBatch();
                        if (++batch>=batchSize) { upd.executeBatch(); batch=0; }
                        changed++;
                    }
                }
            }
            if (batch>0) upd.executeBatch();
            System.out.println("[RewriteFileLinksMigrator] posts updated="+changed);
        }
    }

    private static void rewriteUsers(Connection pg, Map<String,String> fk2id) throws SQLException {
        String select = "SELECT id, avatar FROM users ORDER BY id";
        String update = "UPDATE users SET avatar=?, update_time=? WHERE id=?";
        try (PreparedStatement sel = pg.prepareStatement(select);
             PreparedStatement upd = pg.prepareStatement(update)) {
            Timestamp now = Timestamp.from(Instant.now());
            int batch=0, batchSize=500, changed=0;
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    String avatar = rs.getString(2);
                    String newAvatar = rewriteSingle(avatar, fk2id);
                    if (!eq(avatar,newAvatar)) {
                        int i=1;
                        upd.setString(i++, newAvatar);
                        upd.setTimestamp(i++, now);
                        upd.setString(i++, id);
                        upd.addBatch();
                        if (++batch>=batchSize) { upd.executeBatch(); batch=0; }
                        changed++;
                    }
                }
            }
            if (batch>0) upd.executeBatch();
            System.out.println("[RewriteFileLinksMigrator] users updated="+changed);
        }
    }

    private static String rewriteText(String text, Map<String,String> fk2id) {
        if (text == null || text.isBlank()) return text;
        String out = text;
        // 1) fileKey=xxx（包含URL编码与未编码）
        Matcher m = FILEKEY_PARAM.matcher(out);
        StringBuffer sb = new StringBuffer();
        boolean any=false;
        while (m.find()) {
            String raw = m.group(1);
            String key = urlDecode(raw);
            String id = fk2id.get(key);
            if (id != null) {
                String rep = "/api/public/resource/"+id+"/access";
                m.appendReplacement(sb, Matcher.quoteReplacement(rep));
                any=true;
            }
        }
        m.appendTail(sb);
        out = sb.toString();

        // 2) (digits/xxxx) 仅替换括号内的裸 fileKey
        m = PLAIN_FILEKEY_IN_PARENS.matcher(out);
        sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String id = fk2id.get(key);
            if (id != null) {
                String rep = "("+"/api/public/resource/"+id+"/access"+")";
                m.appendReplacement(sb, Matcher.quoteReplacement(rep));
                any=true;
            }
        }
        m.appendTail(sb);
        out = sb.toString();
        return out;
    }

    private static String rewriteSingle(String value, Map<String,String> fk2id) {
        if (value == null || value.isBlank()) return value;
        // 先尝试 fileKey= 形式
        Matcher m = FILEKEY_PARAM.matcher(value);
        if (m.find()) {
            String key = urlDecode(m.group(1));
            String id = fk2id.get(key);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        // 直接等于 fileKey 的情况
        String id = fk2id.get(value);
        if (id != null) return "/api/public/resource/"+id+"/access";
        return value;
    }

    private static boolean eq(String a, String b) { return (a==null?"":a).equals(b==null?"":b); }
    private static String urlDecode(String s){ try{ return URLDecoder.decode(s, StandardCharsets.UTF_8); }catch(Exception e){ return s; } }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

