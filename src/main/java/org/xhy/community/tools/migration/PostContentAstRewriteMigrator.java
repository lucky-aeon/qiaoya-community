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
 * 使用 Markdown AST 精确重写 posts.content 与 posts.cover_image 中的旧文件链接为新资源访问URL。
 * 旧链接示例：/api/community/file/singUrl?fileKey=31%2F1712487892113
 * 新链接示例：/api/public/resource/{resourceId}/access
 * 前置条件：先运行 FilesToResourcesMigrator，确保 resources(file_key -> id) 映射存在。
 */
public class PostContentAstRewriteMigrator {

    // 只匹配相对路径旧接口，捕获 fileKey 参数值，直到右括号/空白/引号/& 截止
    private static final Pattern OLD_URL = Pattern.compile("/api/community/file/singUrl\\?fileKey=([^)\\s\\\"'&]+)");

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");

        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[PostContentAstRewriteMigrator] Postgres -> " + pgUrl);

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);

            Map<String,String> fk2id = loadFileKeyMap(pg);
            System.out.println("[PostContentAstRewriteMigrator] mappings=" + fk2id.size());

            String select = "SELECT id, content, cover_image FROM posts ORDER BY id";
            String update = "UPDATE posts SET content=?, cover_image=?, update_time=? WHERE id=?";
            try (PreparedStatement sel = pg.prepareStatement(select);
                 PreparedStatement upd = pg.prepareStatement(update)) {
                int changed=0, batch=0, batchSize=300; Timestamp now = Timestamp.from(Instant.now());
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String content = rs.getString(2);
                        String cover = rs.getString(3);
                        String newContent = rewriteMarkdown(content, fk2id);
                        String newCover = mapOldToNew(cover, fk2id);
                        if (!safeEq(content,newContent) || !safeEq(cover,newCover)) {
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
                pg.commit();
                System.out.println("[PostContentAstRewriteMigrator] posts updated="+changed);
            }
        }
    }

    private static String rewriteMarkdown(String md, Map<String,String> fk2id) {
        if (md == null || md.isBlank()) return md;
        MarkdownUrlRewriter rewriter = new MarkdownUrlRewriter();
        String rendered = rewriter.rewrite(md, url -> mapOldToNew(url, fk2id));
        return replacePlainOldUrl(rendered, fk2id);
    }

    private static String mapOldToNew(String url, Map<String,String> fk2id) {
        if (url == null || url.isBlank()) return url;
        // 修复之前错误替换的残留：/api/community/file/singUrl? + 已是新URL
        if (url.contains("/api/community/file/singUrl?")) {
            String after = url.substring(url.indexOf('?') + 1);
            if (after.startsWith("/api/public/resource/")) {
                return after; // 直接去掉旧前缀
            }
            // 如果问号后直接是 fileKey
            if (after.matches("\\d+/[\\w\\-\\.]+")) {
                String id = fk2id.get(after);
                if (id != null) return "/api/public/resource/"+id+"/access";
            }
        }
        // 情况1：旧接口 + fileKey 参数
        Matcher m = OLD_URL.matcher(url);
        if (m.find()) {
            String enc = m.group(1);
            String key = urlDecode(enc);
            String id = fk2id.get(key);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        // 情况2：直接是 fileKey（13/xxxx）
        if (url.matches("\\d+/[\\w\\-\\.]+")) {
            String id = fk2id.get(url);
            if (id != null) return "/api/public/resource/"+id+"/access";
        }
        return url;
    }

    private static boolean safeEq(String a, String b) {
        return (a==null?"":a).equals(b==null?"":b);
    }

    private static String replacePlainOldUrl(String text, Map<String,String> fk2id) {
        if (text == null || text.isBlank()) return text;
        StringBuffer sb = new StringBuffer();
        Matcher m = OLD_URL.matcher(text);
        while (m.find()) {
            String enc = m.group(1);
            String key = urlDecode(enc);
            String id = fk2id.get(key);
            if (id != null) {
                m.appendReplacement(sb, Matcher.quoteReplacement("/api/public/resource/"+id+"/access"));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static Map<String,String> loadFileKeyMap(Connection pg) throws SQLException {
        Map<String,String> map = new HashMap<>();
        try (PreparedStatement ps = pg.prepareStatement("SELECT file_key, id FROM resources")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String k = rs.getString(1);
                    String v = rs.getString(2);
                    if (k!=null && !k.isBlank()) map.put(k,v);
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

