package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 迁移文章：articles(MySQL) -> posts(PostgreSQL)
 * - 分类：直接使用旧表 articles.type 作为 category_id
 * - 文章/问答判定：根据 types 的父类 title 是否为 "QA/问答"
 * - 状态映射：
 *   - 普通文章：Draft->DRAFT，其它->PUBLISHED
 *   - 问答：QADraft/Draft->DRAFT；Pending/Resolved/Published->PUBLISHED；Resolved->resolve_status=SOLVED
 */
public class PostsMigrator {

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
            "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&useCursorFetch=true&connectTimeout=8000&socketTimeout=30000&zeroDateTimeBehavior=convertToNull",
            mysqlHost, mysqlPort, mysqlDb);
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[PostsMigrator] MySQL → " + mysqlUrl);
        System.out.println("[PostsMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String selectSql = "SELECT a.id, a.title, a.content, a.user_id, a.state, a.`like`, a.type, a.top_number, a.cover, a.abstract, a.created_at, a.updated_at, a.deleted_at, tp.title AS parent_title " +
                    "FROM articles a LEFT JOIN types t ON t.id=a.type LEFT JOIN types tp ON tp.id=t.parent_id ORDER BY a.id";
            try (PreparedStatement sel = mysql.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(500);

                String insertSql = "INSERT INTO posts (id, title, content, summary, cover_image, author_id, category_id, status, like_count, view_count, comment_count, is_top, publish_time, create_time, update_time, tags, resolve_status, solved_at, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insertSql)) {
                    int batch=0; final int batchSize=500; long inserted=0; Timestamp now = Timestamp.from(Instant.now());

                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt("id"));
                            String title = rs.getString("title");
                            String content = rs.getString("content");
                            if (content == null) content = "";
                            String authorId = String.valueOf(rs.getInt("user_id"));
                            int state = rs.getInt("state");
                            int likeCount = rs.getInt("like");
                            String categoryId = String.valueOf(rs.getInt("type"));
                            int topNumber = rs.getInt("top_number");
                            String cover = rs.getString("cover");
                            String summary = rs.getString("abstract");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            Timestamp deletedAt = rs.getTimestamp("deleted_at");
                            if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = createdAt;
                            String parentTitle = rs.getString("parent_title");

                            boolean isQA = parentTitle != null && ("QA".equalsIgnoreCase(parentTitle) || "问答".equals(parentTitle));
                            String status = mapStatus(isQA, state);
                            String resolveStatus = isQA ? mapResolveStatus(state) : null;
                            Timestamp solvedAt = (isQA && "SOLVED".equals(resolveStatus)) ? (updatedAt != null? updatedAt : createdAt) : null;
                            boolean isTop = topNumber > 0;
                            Timestamp publishTime = "PUBLISHED".equals(status) ? createdAt : null;
                            String tagsJson = toTagsJson(mysql, rs.getInt("id"));

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, title);
                            ins.setString(i++, content);
                            ins.setString(i++, summary);
                            ins.setString(i++, cover);
                            ins.setString(i++, authorId);
                            ins.setString(i++, categoryId);
                            ins.setString(i++, status);
                            ins.setInt(i++, likeCount);
                            ins.setInt(i++, 0); // view_count unknown
                            ins.setInt(i++, 0); // comment_count unknown
                            ins.setBoolean(i++, isTop);
                            if (publishTime != null) ins.setTimestamp(i++, publishTime); else ins.setNull(i++, Types.TIMESTAMP);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            if (tagsJson != null) ins.setObject(i++, tagsJson, Types.OTHER); else ins.setNull(i++, Types.OTHER);
                            if (resolveStatus != null) ins.setString(i++, resolveStatus); else ins.setNull(i++, Types.VARCHAR);
                            if (solvedAt != null) ins.setTimestamp(i++, solvedAt); else ins.setNull(i++, Types.TIMESTAMP);
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                    }
                    if (batch > 0) inserted += flush(ins, pg);
                    System.out.printf("[PostsMigrator] inserted=%d\n", inserted);
                }
            }
        }
    }

    private static String mapStatus(boolean isQA, int state) {
        // 旧常量：Draft=1, Published=2, Pending=3, Resolved=4, PrivateQuestion=5, QADraft=6
        if (isQA) {
            if (state == 1 || state == 6) return "DRAFT";
            return "PUBLISHED"; // Pending/Resolved/Published -> PUBLISHED
        } else {
            return state == 1 ? "DRAFT" : "PUBLISHED";
        }
    }

    private static String mapResolveStatus(int state) {
        return state == 4 ? "SOLVED" : "UNSOLVED"; // Resolved -> SOLVED，其余当作未解决
    }

    private static String toTagsJson(Connection mysql, int articleId) throws SQLException {
        String sql = "SELECT at.tag_name FROM article_tag_relations r JOIN article_tags at ON r.tag_id=at.id WHERE r.article_id=?";
        try (PreparedStatement ps = mysql.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> tags = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (name != null && !name.isBlank()) tags.add(escapeJson(name));
                }
                if (tags.isEmpty()) return null;
                StringBuilder sb = new StringBuilder("[");
                for (int i=0;i<tags.size();i++) {
                    if (i>0) sb.append(',');
                    sb.append('"').append(tags.get(i)).append('"');
                }
                sb.append(']');
                return sb.toString();
            }
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static long flush(PreparedStatement ins, Connection pg) throws SQLException {
        int[] counts = ins.executeBatch(); pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c== Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
