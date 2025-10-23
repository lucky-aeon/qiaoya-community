package org.xhy.community.tools.migration;

import org.xhy.community.infrastructure.markdown.MarkdownParser;
import org.xhy.community.infrastructure.markdown.impl.FlexmarkMarkdownParser;
import org.xhy.community.infrastructure.markdown.model.MarkdownNode;
import org.xhy.community.infrastructure.markdown.model.NodeType;

import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * 章节内容资源绑定回填/重建脚本
 *
 * 功能：
 * 1) 遍历 chapters.content（Markdown），使用项目内 AST 解析器提取图片与视频资源 URL
 * 2) 从 URL 中解析资源ID（/api/public/resource/{id}/...）
 * 3) 先软删该章节的旧绑定（deleted_at=NOW()），再为提取到的资源 ID 批量创建 resource_bindings（target_type=CHAPTER）
 *
 * 环境变量（沿用现有迁移工具约定）：
 * - TARGET_PG_HOST (默认 124.220.234.136)
 * - TARGET_PG_PORT (默认 5432)
 * - TARGET_PG_DB   (默认 qiaoya_community)
 * - TARGET_PG_USER (默认取 DB_USERNAME 或 qiaoya_community)
 * - TARGET_PG_PASS (默认取 DB_PASSWORD；必填)
 *
 * 可选控制：
 * - ONLY_CHAPTER_ID 仅处理指定章节ID
 */
public class ChapterResourceBindingsMigrator {

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("DB_NAME", "qiaoya_community");
        String pgUser = env("DB_USERNAME", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("DB_PASSWORD", env("DB_PASSWORD", null));
        String onlyChapterId = env("ONLY_CHAPTER_ID", null);

        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[ChapterResourceBindingsMigrator] Postgres -> " + pgUrl);
        if (onlyChapterId != null) System.out.println("[ChapterResourceBindingsMigrator] ONLY_CHAPTER_ID=" + onlyChapterId);

        MarkdownParser parser = new FlexmarkMarkdownParser();

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);

            // 预加载 resources.id 集合，避免绑定不存在的资源
            Set<String> resourceIds = loadResourceIds(pg);
            System.out.println("[ChapterResourceBindingsMigrator] resources loaded=" + resourceIds.size());

            String select = onlyChapterId == null
                    ? "SELECT id, content FROM chapters ORDER BY id"
                    : "SELECT id, content FROM chapters WHERE id=?";

            try (PreparedStatement sel = pg.prepareStatement(select)) {
                if (onlyChapterId != null) sel.setString(1, onlyChapterId);
                try (ResultSet rs = sel.executeQuery()) {
                    long processed = 0, totalBindings = 0;
                    while (rs.next()) {
                        String chapterId = rs.getString(1);
                        String content = rs.getString(2);

                        MarkdownNode root = parser.parse(content);
                        Set<String> ids = extractResourceIds(root);
                        // 仅保留存在于 resources 的 id
                        ids.removeIf(id -> !resourceIds.contains(id));

                        // 先软删再新建
                        int deleted = softDeleteBindingsForChapter(pg, chapterId);
                        int inserted = insertBindingsForChapter(pg, chapterId, ids);

                        processed++;
                        totalBindings += inserted;
                        if (processed % 50 == 0) pg.commit();
                        System.out.printf("[ChapterResourceBindingsMigrator] chapter=%s deleted=%d inserted=%d\n", chapterId, deleted, inserted);
                    }
                    pg.commit();
                    System.out.printf("[ChapterResourceBindingsMigrator] done. chapters=%d, new_bindings=%d\n", processed, totalBindings);
                }
            }
        }
    }

    private static Set<String> loadResourceIds(Connection pg) throws SQLException {
        Set<String> set = new HashSet<>();
        try (PreparedStatement ps = pg.prepareStatement("SELECT id FROM resources WHERE deleted_at IS NULL")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getString(1));
            }
        }
        return set;
    }

    private static int softDeleteBindingsForChapter(Connection pg, String chapterId) throws SQLException {
        String sql = "UPDATE resource_bindings SET deleted_at=NOW(), update_time=NOW() " +
                "WHERE target_type='CHAPTER' AND target_id=? AND deleted_at IS NULL";
        try (PreparedStatement ps = pg.prepareStatement(sql)) {
            ps.setString(1, chapterId);
            return ps.executeUpdate();
        }
    }

    private static int insertBindingsForChapter(Connection pg, String chapterId, Set<String> resourceIds) throws SQLException {
        if (resourceIds == null || resourceIds.isEmpty()) return 0;
        // 注意：resource_bindings 上的唯一约束通过一个带 WHERE deleted_at IS NULL 的部分唯一索引实现。
        // Postgres 不支持为带 WHERE 的部分索引使用列清单进行 ON CONFLICT 推断，因此这里使用无目标的
        // ON CONFLICT DO NOTHING，让任何唯一/排他约束冲突都忽略，从而复用该部分唯一索引。
        String sql = "INSERT INTO resource_bindings (id, resource_id, target_type, target_id, create_time, update_time, deleted_at) " +
                "VALUES (?,?,?,?,?,?,NULL) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = pg.prepareStatement(sql)) {
            int batch = 0, batchSize = 500, total = 0; Timestamp now = Timestamp.from(Instant.now());
            for (String rid : resourceIds) {
                int i=1;
                ps.setString(i++, UUID.randomUUID().toString());
                ps.setString(i++, rid);
                ps.setString(i++, "CHAPTER");
                ps.setString(i++, chapterId);
                ps.setTimestamp(i++, now);
                ps.setTimestamp(i++, now);
                ps.addBatch();
                if (++batch >= batchSize) { total += flush(ps, pg); batch = 0; }
            }
            if (batch > 0) total += flush(ps, pg);
            return total;
        }
    }

    /**
     * 遍历 AST，提取 /api/public/resource/{id}/... 中的 id；
     * - 支持 IMAGE 与 VIDEO（VIDEO 同时检查 src 与 poster）
     * - id 提取策略：截取 "/api/public/resource/" 之后到下一个 '/' 之间的内容
     */
    static Set<String> extractResourceIds(MarkdownNode root) {
        Set<String> ids = new HashSet<>();
        if (root == null) return ids;
        visit(root, ids);
        return ids;
    }

    private static void visit(MarkdownNode node, Set<String> out) {
        if (node.getType() == NodeType.IMAGE) {
            String src = node.getAttributes().get("src");
            String id = extractIdFromUrl(src);
            if (id != null) out.add(id);
        } else if (node.getType() == NodeType.VIDEO) {
            String src = node.getAttributes().get("src");
            String poster = node.getAttributes().get("poster");
            String id1 = extractIdFromUrl(src);
            String id2 = extractIdFromUrl(poster);
            if (id1 != null) out.add(id1);
            if (id2 != null) out.add(id2);
        }
        if (node.getChildren() != null) {
            for (MarkdownNode c : node.getChildren()) {
                visit(c, out);
            }
        }
    }

    private static String extractIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        int idx = url.indexOf("/api/public/resource/");
        if (idx < 0) return null;
        int start = idx + "/api/public/resource/".length();
        int end = url.indexOf('/', start);
        if (end < 0) return null;
        String id = url.substring(start, end);
        // 接受 UUID(36/32) 或 纯数字 ID；其它忽略
        if (id.matches("[A-Fa-f0-9\\-]{36}") || id.matches("[A-Fa-f0-9]{32}") || id.matches("\\d+")) {
            return id;
        }
        return null;
    }

    private static long flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch();
        pg.commit();
        long ok = 0; for (int c : counts) if (c > 0 || c == Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
