package org.xhy.community.tools.migration;

import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 生成用户与套餐的订阅关联（全员绑定+有订单/使用码的用户按时间生成）
 * - plan: e184abe9bb836471ffcd1c52e8d19bdf
 * - 不重复：ON CONFLICT(user_id, subscription_plan_id) DO NOTHING
 */
public class UserSubscriptionsMigrator {

    private static final String PLAN_ID = "e184abe9bb836471ffcd1c52e8d19bdf";

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
            "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&useCursorFetch=true&connectTimeout=8000&socketTimeout=30000",
            mysqlHost, mysqlPort, mysqlDb);
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[UserSubscriptionsMigrator] MySQL → " + mysqlUrl);
        System.out.println("[UserSubscriptionsMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            // 校验套餐存在
            Plan plan = readPlan(pg, PLAN_ID);
            if (plan == null) throw new IllegalStateException("subscription_plans not found: " + PLAN_ID);

            pg.setAutoCommit(false);

            String insertSql = "INSERT INTO user_subscriptions (id, user_id, subscription_plan_id, start_time, end_time, status, cdk_code, create_time, update_time, deleted_at) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
            try (PreparedStatement ins = pg.prepareStatement(insertSql)) {
                long inserted = 0;

                // 统一口径：开始时间=当前时间，结束时间=1年后，创建时间=当前时间
                Timestamp now = Timestamp.from(Instant.now());
                Timestamp endOneYear = addOneYear(now);

                // 1) 有订单用户（cdk_code 取旧 invite_code，时间按统一口径）
                String selectOrderUsers = "SELECT purchaser, invite_code, created_at FROM orders ORDER BY id";
                try (PreparedStatement sel = mysql.prepareStatement(selectOrderUsers, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                    sel.setFetchSize(1000);
                    int batch = 0; final int batchSize = 1000;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String userId = String.valueOf(rs.getInt("purchaser"));
                            String code = rs.getString("invite_code");
                            Timestamp startTs = now;
                            Timestamp endTs = endOneYear;

                            int i = 1;
                            ins.setString(i++, generateId(userId));
                            ins.setString(i++, userId);
                            ins.setString(i++, PLAN_ID);
                            ins.setTimestamp(i++, startTs);
                            ins.setTimestamp(i++, endTs);
                            ins.setString(i++, "ACTIVE");
                            ins.setString(i++, code);
                            ins.setTimestamp(i++, now);    // create_time
                            ins.setTimestamp(i++, now);    // update_time
                            ins.setNull(i++, Types.TIMESTAMP);
                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }
                        }
                    }
                    if (batch > 0) inserted += flush(ins, pg);
                }

                // 2) 其余用户（全员绑定，start=now，end=now+1年，cdk_code=null）
                String selectPgUsers = "SELECT id FROM users";
                try (PreparedStatement sel = pg.prepareStatement(selectPgUsers)) {
                    try (ResultSet rs = sel.executeQuery()) {
                        int batch = 0; final int batchSize = 1000;
                        while (rs.next()) {
                            String userId = rs.getString(1);
                            int i = 1;
                            ins.setString(i++, generateId(userId));
                            ins.setString(i++, userId);
                            ins.setString(i++, PLAN_ID);
                            ins.setTimestamp(i++, now);
                            ins.setTimestamp(i++, endOneYear);
                            ins.setString(i++, "ACTIVE");
                            ins.setNull(i++, Types.VARCHAR);
                            ins.setTimestamp(i++, now);
                            ins.setTimestamp(i++, now);
                            ins.setNull(i++, Types.TIMESTAMP);
                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }
                        }
                        if (batch > 0) inserted += flush(ins, pg);
                    }
                }

                System.out.printf("[UserSubscriptionsMigrator] Done. inserted=%d\n", inserted);
            }
        }
    }

    private static class Plan { String name; int validityMonths; }

    private static Plan readPlan(Connection pg, String planId) throws SQLException {
        try (PreparedStatement ps = pg.prepareStatement("SELECT name, validity_months FROM subscription_plans WHERE id=?")) {
            ps.setString(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { Plan p = new Plan(); p.name = rs.getString(1); p.validityMonths = rs.getInt(2); return p; }
                return null;
            }
        }
    }

    private static Timestamp addMonths(Timestamp ts, int months) {
        LocalDateTime ldt = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()).plusMonths(months);
        return Timestamp.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static Timestamp addOneYear(Timestamp ts) {
        LocalDateTime ldt = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()).plusYears(1);
        return Timestamp.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static String generateId(String userId) {
        // 生成稳定且不超过36字符的ID：md5(userId:PLAN_ID) -> 32位hex
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest((userId + ":" + PLAN_ID).getBytes());
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            // 退化方案：截断拼接字符串
            String s = ("usub-" + userId + "-" + PLAN_ID);
            return s.length() <= 36 ? s : s.substring(0, 36);
        }
    }

    private static long flush(PreparedStatement ins, Connection pg) throws SQLException {
        int[] counts = ins.executeBatch(); pg.commit();
        long ok = 0; for (int c : counts) if (c > 0 || c == Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
