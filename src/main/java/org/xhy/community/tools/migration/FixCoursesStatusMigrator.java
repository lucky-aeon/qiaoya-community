package org.xhy.community.tools.migration;

import java.sql.*;

/**
 * 按最新口径修正已迁移课程的状态：
 * - 旧库 state=1 -> 新库 status=IN_PROGRESS
 * - 旧库 state=2 -> 新库 status=COMPLETED
 * - 其他/空 -> PENDING
 * 通过对照旧库 courses(id,state) 更新新库 courses.status，避免重复删除重迁。
 */
public class FixCoursesStatusMigrator {

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

        System.out.println("[FixCoursesStatusMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[FixCoursesStatusMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);
            String select = "SELECT id, state FROM courses";
            String update = "UPDATE courses SET status=?, update_time=now() WHERE id=?";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                 PreparedStatement upd = pg.prepareStatement(update)) {
                sel.setFetchSize(500);
                int updated = 0, batch = 0, batchSize = 500;
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        String id = String.valueOf(rs.getInt(1));
                        Integer state = toInt(rs.getObject(2));
                        String status = mapStatus(state);
                        upd.setString(1, status);
                        upd.setString(2, id);
                        upd.addBatch();
                        if (++batch >= batchSize) { upd.executeBatch(); batch = 0; }
                        updated++;
                    }
                }
                if (batch > 0) upd.executeBatch();
                pg.commit();
                System.out.println("[FixCoursesStatusMigrator] updated=" + updated);
            }
        }
    }

    private static String mapStatus(Integer state) {
        if (state != null) {
            if (state == 2) return "COMPLETED";
            if (state == 1) return "IN_PROGRESS";
        }
        return "PENDING";
    }

    private static Integer toInt(Object o) {
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
