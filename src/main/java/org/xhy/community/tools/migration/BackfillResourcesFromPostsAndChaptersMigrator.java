package org.xhy.community.tools.migration;

import org.xhy.community.infrastructure.markdown.MarkdownUrlRewriter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修复 posts/chapters 中旧格式文件链接：
 * 1) 扫描 content 中的 /api/community/file/singUrl?fileKey=... 及裸 fileKey（13/xxxx）
 * 2) 对于 resources 表缺失的 file_key，补齐最小资源记录（UUID、size=0、format=""、user_id="0"、resource_type=OTHER、original_name=最后一段）
 * 3) 回写 posts.content 与 chapters.content 为新URL：/api/public/resource/{id}/access
 *
 * 注意：本脚本只面向内容中的资源修复，不处理封面、头像等，若需要请使用已有脚本（如 RewriteFileLinksMigrator/FixPostCoverToResourceIdMigrator）。
 */
public class BackfillResourcesFromPostsAndChaptersMigrator {

    private static final Pattern OLD_URL = Pattern.compile("/api/community/file/singUrl\\?fileKey=([^)\\s\\\"'&]+)");
    private static final Pattern PLAIN_FILEKEY_IN_PARENS = Pattern.compile("\\((\\d+/[\\w\\-\\.]+)\\)");

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");

        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[BackfillResourcesFromPostsAndChaptersMigrator] Postgres -> " + pgUrl);

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);

            // 1) 载入已存在的 fileKey -> id 映射
            Map<String, String> fk2id = loadFileKeyMap(pg);
            System.out.println("[Backfill] existing resources=" + fk2id.size());

            // 2) 扫描 posts/chapters 内容中的 fileKey
            Set<String> discovered = new HashSet<>();
            scanTableForFileKeys(pg, "posts", discovered);
            scanTableForFileKeys(pg, "chapters", discovered);
            System.out.println("[Backfill] discovered fileKeys in content=" + discovered.size());

            // 3) 补齐 resources 表缺失的数据
            long inserted = insertMissingResources(pg, discovered, fk2id);
            System.out.println("[Backfill] resources inserted=" + inserted);

            // 4) 回写内容链接为新URL（利用 AST + 兜底正则）
            int p = rewriteContent(pg, "posts", fk2id);
            int c = rewriteContent(pg, "chapters", fk2id);
            pg.commit();
            System.out.printf("[Backfill] rewritten posts=%d, chapters=%d\n", p, c);
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

    private static void scanTableForFileKeys(Connection pg, String table, Set<String> out) throws SQLException {
        String sql = "SELECT id, content FROM " + table + " ORDER BY id";
        try (PreparedStatement ps = pg.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String content = rs.getString(2);
                    collectFileKeys(content, out);
                }
            }
        }
    }

    private static void collectFileKeys(String text, Set<String> out) {
        if (text == null || text.isBlank()) return;
        // 1) /api/community/file/singUrl?fileKey=...
        Matcher m = OLD_URL.matcher(text);
        while (m.find()) {
            String enc = m.group(1);
            String key = urlDecode(enc);
            if (key != null && !key.isBlank()) out.add(key);
        }
        // 2) 括号内裸 fileKey
        m = PLAIN_FILEKEY_IN_PARENS.matcher(text);
        while (m.find()) {
            String key = m.group(1);
            if (key != null && !key.isBlank()) out.add(key);
        }
    }

    private static long insertMissingResources(Connection pg, Set<String> discovered, Map<String,String> fk2id) throws SQLException {
        String insert = "INSERT INTO resources (id, file_key, size, format, user_id, resource_type, original_name, create_time, update_time, deleted_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,NULL) ON CONFLICT (file_key) DO NOTHING";
        try (PreparedStatement ins = pg.prepareStatement(insert)) {
            int batch = 0, batchSize = 500; long inserted = 0; Timestamp now = Timestamp.from(Instant.now());
            for (String fk : discovered) {
                if (fk2id.containsKey(fk)) continue; // 已存在
                String id = UUID.randomUUID().toString();
                String originalName = deriveOriginalName(fk);
                String ext = deriveExtension(originalName);
                String resourceType = classifyType(ext);

                int i=1;
                ins.setString(i++, id);
                ins.setString(i++, fk);
                ins.setLong(i++, 0L);
                ins.setString(i++, ext == null ? "" : ext);
                ins.setString(i++, "0"); // 无法追溯归属，记为 0
                ins.setString(i++, resourceType);
                ins.setString(i++, originalName == null ? fk : originalName);
                ins.setTimestamp(i++, now);
                ins.setTimestamp(i++, now);
                ins.addBatch();
                if (++batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }

                // 更新内存映射用于随后的重写
                fk2id.put(fk, id);
            }
            if (batch > 0) inserted += flush(ins, pg);
            return inserted;
        }
    }

    private static int rewriteContent(Connection pg, String table, Map<String,String> fk2id) throws SQLException {
        String select = "SELECT id, content FROM " + table + " ORDER BY id";
        String update = "UPDATE " + table + " SET content=?, update_time=? WHERE id=?";
        MarkdownUrlRewriter rewriter = new MarkdownUrlRewriter();
        try (PreparedStatement sel = pg.prepareStatement(select);
             PreparedStatement upd = pg.prepareStatement(update)) {
            int changed=0, batch=0, batchSize=500; Timestamp now = Timestamp.from(Instant.now());
            try (ResultSet rs = sel.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    String content = rs.getString(2);
                    String newContent = rewriteMarkdown(rewriter, content, fk2id);
                    if (!safeEq(content, newContent)) {
                        int i=1;
                        upd.setString(i++, newContent);
                        upd.setTimestamp(i++, now);
                        upd.setString(i++, id);
                        upd.addBatch();
                        if (++batch >= batchSize) { upd.executeBatch(); batch = 0; }
                        changed++;
                    }
                }
            }
            if (batch > 0) upd.executeBatch();
            return changed;
        }
    }

    private static String rewriteMarkdown(MarkdownUrlRewriter rewriter, String md, Map<String,String> fk2id) {
        if (md == null || md.isBlank()) return md;
        String rendered = rewriter.rewrite(md, url -> mapOldToNew(url, fk2id));
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

    private static long flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch();
        pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String deriveOriginalName(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) return null;
        int slash = fileKey.lastIndexOf('/');
        String name = slash >= 0 ? fileKey.substring(slash + 1) : fileKey;
        return name.isEmpty() ? null : name;
    }

    private static String deriveExtension(String name) {
        if (name == null) return null;
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length()-1) return ""; // 无扩展名
        return name.substring(dot + 1).toLowerCase();
    }

    private static String classifyType(String ext) {
        if (ext == null) return "OTHER";
        String e = ext.toLowerCase();
        if (e.matches("jpg|jpeg|png|gif|bmp|webp|svg")) return "IMAGE";
        if (e.matches("mp4|avi|mov|wmv|flv|webm|mkv")) return "VIDEO";
        if (e.matches("mp3|wav|flac|aac|ogg|wma")) return "AUDIO";
        if (e.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf")) return "DOCUMENT";
        return "OTHER";
    }

    private static boolean safeEq(String a, String b) { return (a==null?"":a).equals(b==null?"":b); }
    private static String urlDecode(String s) { try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; } }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
