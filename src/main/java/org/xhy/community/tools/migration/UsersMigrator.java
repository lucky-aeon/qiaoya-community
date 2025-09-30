package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 简单的“users”表迁移工具：直连 MySQL 旧库读取，写入 PostgreSQL 新库。
 * 主键直接沿用旧库自增 ID（以字符串形式插入 PG 的 varchar 主键）。
 *
 * 运行前请设置环境变量（未设置则使用合理默认）：
 * - LEGACY_MYSQL_HOST（默认 124.220.234.136）
 * - LEGACY_MYSQL_PORT（默认 3306）
 * - LEGACY_MYSQL_DB（默认 community）
 * - LEGACY_MYSQL_USER（默认 community）
 * - LEGACY_MYSQL_PASS（必填或从 DB_PASS 兜底）
 * - TARGET_PG_HOST（默认 124.220.234.136）
 * - TARGET_PG_PORT（默认 5432）
 * - TARGET_PG_DB（默认 qiaoya_community）
 * - TARGET_PG_USER（默认 qiaoya_community）
 * - TARGET_PG_PASS（必填或从 DB_PASSWORD 兜底）
 *
 * 执行：
 *   mvn -q -DskipTests exec:java
 */
public class UsersMigrator {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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

        if (mysqlPass == null || mysqlPass.isEmpty()) {
            throw new IllegalArgumentException("Missing LEGACY_MYSQL_PASS or DB_PASS for MySQL");
        }
        if (pgPass == null || pgPass.isEmpty()) {
            throw new IllegalArgumentException("Missing TARGET_PG_PASS or DB_PASSWORD for Postgres");
        }

        String mysqlUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&useCursorFetch=true&zeroDateTimeBehavior=convertToNull",
                mysqlHost, mysqlPort, mysqlDb);

        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[UsersMigrator] MySQL → " + mysqlUrl);
        System.out.println("[UsersMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String selectSql = "SELECT id, name, account, password, `desc` AS description, avatar, subscribe, state, " +
                    "max_concurrent_devices, created_at, updated_at, deleted_at FROM users";
            try (PreparedStatement sel = mysql.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);
                long total = 0, inserted = 0;

                String insertSql = "INSERT INTO users (id, name, description, avatar, email, password, status, " +
                        "email_notification_enabled, max_concurrent_devices, create_time, update_time, role, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insertSql)) {
                    int batch = 0;
                    final int batchSize = 1000;
                    Timestamp now = Timestamp.from(Instant.now());

                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            total++;

                            long oldId = rs.getLong("id");
                            String idStr = String.valueOf(oldId);

                            String name = rs.getString("name");
                            if (name == null || name.isBlank()) name = "用户" + idStr;

                            String account = rs.getString("account");
                            String email = toEmail(account, idStr);

                            String password = rs.getString("password");
                            if (password == null) password = ""; // 兜底，避免 NOT NULL 冲突

                            String description = rs.getString("description");
                            String avatar = rs.getString("avatar");

                            Integer state = (Integer) rs.getObject("state");
                            // 旧项目用户状态：常用 1=正常、2=拉黑；0 在代码中未使用，视为历史默认值，按“正常”处理
                            String status = "ACTIVE"; // 默认按正常
                            if (state != null) {
                                if (state == 2) {
                                    status = "BANNED";
                                } else {
                                    status = "ACTIVE"; // 包含 0/1/其他均按正常
                                }
                            }

                            Integer subscribe = (Integer) rs.getObject("subscribe");
                            boolean emailNotificationEnabled = (subscribe != null && subscribe == 2);

                            Integer mcd = (Integer) rs.getObject("max_concurrent_devices");
                            if (mcd == null) mcd = 1;
                            if (mcd < 1) mcd = 1;
                            if (mcd > 10) mcd = 10;

                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            Timestamp deletedAt = rs.getTimestamp("deleted_at");
                            if (createdAt == null) createdAt = now;
                            if (updatedAt == null) updatedAt = createdAt;

                            int i = 1;
                            ins.setString(i++, idStr);
                            ins.setString(i++, name);
                            ins.setString(i++, description);
                            ins.setString(i++, avatar);
                            ins.setString(i++, email);
                            ins.setString(i++, password);
                            ins.setString(i++, status);
                            ins.setBoolean(i++, emailNotificationEnabled); // email_notification_enabled
                            ins.setInt(i++, mcd);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            ins.setString(i++, "USER"); // role
                            ins.setTimestamp(i++, deletedAt);

                            ins.addBatch();
                            batch++;

                            if (batch >= batchSize) {
                                inserted += flushBatch(ins, pg);
                                batch = 0;
                            }
                        }
                    }

                    if (batch > 0) {
                        inserted += flushBatch(ins, pg);
                    }

                    System.out.printf("[UsersMigrator] Done. total=%d, inserted=%d\n", total, inserted);
                }
            }
        }
    }

    private static long flushBatch(PreparedStatement ins, Connection pg) throws SQLException {
        long inserted = 0;
        int[] counts = ins.executeBatch();
        pg.commit();
        for (int c : counts) {
            if (c > 0 || c == Statement.SUCCESS_NO_INFO) inserted++;
        }
        System.out.printf("[UsersMigrator] batch committed, inserted=%d\n", inserted);
        return inserted;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private static String toEmail(String account, String idStr) {
        if (account != null) {
            String a = account.trim();
            if (EMAIL.matcher(a).matches()) return a.toLowerCase();
        }
        return "user_" + idStr + "@legacy.local";
    }
}
