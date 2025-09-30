package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;

/**
 * 迁移分类：types(MySQL) -> categories(PostgreSQL)
 * - 顶级分类 title=="QA" -> type=QA，其它 -> ARTICLE
 * - 子级分类继承父级的 type
 * - level: 顶级=1，子级=2
 */
public class CategoriesMigrator {

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

        System.out.println("[CategoriesMigrator] MySQL → " + mysqlUrl);
        System.out.println("[CategoriesMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            // 先写顶级分类（parent_id=0）
            migrate(mysql, pg, true);
            // 再写子级分类
            migrate(mysql, pg, false);
        }
    }

    private static void migrate(Connection mysql, Connection pg, boolean topLevel) throws SQLException {
        String where = topLevel ? "WHERE (parent_id = 0 OR parent_id IS NULL)" : "WHERE (parent_id <> 0 AND parent_id IS NOT NULL)";
        String sql = "SELECT id, parent_id, title, `desc`, state, sort, created_at, updated_at, deleted_at FROM types " + where + " ORDER BY id";
        try (PreparedStatement sel = mysql.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            sel.setFetchSize(1000);

            String insSql = "INSERT INTO categories (id, name, parent_id, type, level, sort_order, description, icon, is_active, create_time, update_time, deleted_at) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
            try (PreparedStatement ins = pg.prepareStatement(insSql)) {
                int batch = 0; final int batchSize = 1000; long inserted=0; Timestamp now = Timestamp.from(Instant.now());
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        String id = String.valueOf(rs.getInt("id"));
                        int parentId = rs.getInt("parent_id");
                        boolean isTop = parentId == 0;
                        String name = rs.getString("title");
                        String desc = rs.getString("desc");
                        Object stateObj = rs.getObject("state");
                        Integer state = toInt(stateObj);
                        Integer sort = (Integer) rs.getObject("sort");
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        Timestamp updatedAt = rs.getTimestamp("updated_at");
                        Timestamp deletedAt = rs.getTimestamp("deleted_at");
                        if (createdAt == null) createdAt = now;
                        if (updatedAt == null) updatedAt = createdAt;

                        String parentIdStr = isTop ? null : String.valueOf(parentId);
                        int level = isTop ? 1 : 2;
                        boolean isActive = (state == null) || (state == 1);
                        String type = resolveType(mysql, parentId, name, isTop);

                        int i = 1;
                        ins.setString(i++, id);
                        ins.setString(i++, name);
                        ins.setString(i++, parentIdStr);
                        ins.setString(i++, type);
                        ins.setInt(i++, level);
                        ins.setInt(i++, sort == null ? 0 : sort);
                        ins.setString(i++, desc);
                        ins.setString(i++, null); // icon
                        ins.setBoolean(i++, isActive);
                        ins.setTimestamp(i++, createdAt);
                        ins.setTimestamp(i++, updatedAt);
                        if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                        ins.addBatch();
                        if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }
                    }
                }
                if (batch > 0) inserted += flush(ins, pg);
                System.out.printf("[CategoriesMigrator] topLevel=%s inserted=%d\n", topLevel, inserted);
            }
        }
    }

    private static String resolveType(Connection mysql, int parentId, String name, boolean isTop) throws SQLException {
        if (isTop) {
            return "QA".equalsIgnoreCase(name) || "问答".equals(name) ? "QA" : "ARTICLE";
        }
        try (PreparedStatement ps = mysql.prepareStatement("SELECT title FROM types WHERE id=?")) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String p = rs.getString(1);
                    return ("QA".equalsIgnoreCase(p) || "问答".equals(p)) ? "QA" : "ARTICLE";
                }
            }
        }
        // fallback
        return "ARTICLE";
    }

    private static long flush(PreparedStatement ins, Connection pg) throws SQLException {
        int[] counts = ins.executeBatch(); pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c== Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Short) return ((Short) o).intValue();
        if (o instanceof Byte) return ((Byte) o).intValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0;
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception ignore) { return null; }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
