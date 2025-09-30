package org.xhy.community.tools.migration;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;

/**
 * 迁移激活码：invite_codes(+users/invite_relations/orders) -> cdk_codes
 * 规则：
 * - acquisition_type: 1=GIFT, 2=PURCHASE
 * - status: 若有使用记录或 state==1 则 USED，否则 ACTIVE
 * - cdk_type 固定 SUBSCRIPTION_PLAN，target_id=固定套餐ID
 */
public class CdkCodesMigrator {

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
            "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&useCursorFetch=true&connectTimeout=8000&socketTimeout=30000&zeroDateTimeBehavior=convertToNull",
            mysqlHost, mysqlPort, mysqlDb);
        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);

        System.out.println("[CdkCodesMigrator] MySQL → " + mysqlUrl);
        System.out.println("[CdkCodesMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            // 校验套餐存在
            if (!planExists(pg, PLAN_ID)) {
                throw new IllegalStateException("subscription_plans not found: " + PLAN_ID);
            }

            pg.setAutoCommit(false);

            String selectSql = "SELECT id, code, member_id, state, acquisition_type, creator, created_at, updated_at FROM invite_codes ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String insertSql = "INSERT INTO cdk_codes (id, code, cdk_type, target_id, status, used_by_user_id, used_time, create_time, update_time, acquisition_type, remark, price, subscription_strategy, batch_id, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insertSql)) {
                    long total = 0, inserted = 0; int batch = 0; final int batchSize = 1000;
                    Timestamp now = Timestamp.from(Instant.now());

                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            total++;
                            long id = rs.getLong("id");
                            String idStr = String.valueOf(id);
                            String code = rs.getString("code");
                            Object stateObj = rs.getObject("state");
                            Integer state = toInt(stateObj);
                            Object acqObj = rs.getObject("acquisition_type");
                            Integer acq = toInt(acqObj);
                            Object creatorObj = rs.getObject("creator");
                            Integer creator = toInt(creatorObj);
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            if (createdAt == null) createdAt = now;
                            if (updatedAt == null) updatedAt = createdAt;

                            // 使用关系（优先 invite_relations，其次 users.invite_code，其次 orders）
                            UsedInfo used = findUsedInfo(mysql, code);
                            String status = ((state != null && state == 1) || used.userId != null) ? "USED" : "ACTIVE";
                            String acquisitionType = (acq != null && acq == 1) ? "GIFT" : "PURCHASE"; // 1=赠送 2=购买

                            int i = 1;
                            ins.setString(i++, idStr);
                            ins.setString(i++, code);
                            ins.setString(i++, "SUBSCRIPTION_PLAN");
                            ins.setString(i++, PLAN_ID);
                            ins.setString(i++, status);
                            if (used.userId != null) {
                                ins.setString(i++, used.userId);
                            } else {
                                ins.setNull(i++, Types.VARCHAR);
                            }
                            ins.setTimestamp(i++, used.usedTime);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            ins.setString(i++, acquisitionType);
                            ins.setString(i++, creator == null ? null : ("creator=" + creator));
                            if (used.orderPrice != null) {
                                ins.setBigDecimal(i++, used.orderPrice);
                            } else {
                                ins.setNull(i++, Types.NUMERIC);
                            }
                            ins.setNull(i++, Types.VARCHAR); // subscription_strategy
                            ins.setNull(i++, Types.VARCHAR); // batch_id
                            ins.setNull(i++, Types.TIMESTAMP); // deleted_at

                            ins.addBatch();
                            batch++;
                            if (batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }
                        }
                    }

                    if (batch > 0) inserted += flush(ins, pg);
                    System.out.printf("[CdkCodesMigrator] Done. total=%d, inserted=%d\n", total, inserted);
                }
            }
        }
    }

    private static class UsedInfo {
        String userId; Timestamp usedTime; BigDecimal orderPrice;
    }

    private static UsedInfo findUsedInfo(Connection mysql, String code) throws SQLException {
        UsedInfo info = new UsedInfo();
        // invite_relations（优先）
        try (PreparedStatement ps = mysql.prepareStatement(
                "SELECT invitee_id, created_at FROM invite_relations WHERE invite_code=? ORDER BY created_at ASC LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.userId = String.valueOf(rs.getInt(1));
                    info.usedTime = rs.getTimestamp(2);
                }
            }
        }
        // 若无，则从 users.invite_code 推断
        if (info.userId == null) {
            try (PreparedStatement ps = mysql.prepareStatement(
                    "SELECT id, created_at FROM users WHERE invite_code=? LIMIT 1")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        info.userId = String.valueOf(rs.getInt(1));
                        info.usedTime = rs.getTimestamp(2);
                    }
                }
            }
        }
        // 订单价格（若存在）
        try (PreparedStatement ps = mysql.prepareStatement(
                "SELECT price FROM orders WHERE invite_code=? ORDER BY created_at ASC LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String p = rs.getString(1);
                    if (p != null) info.orderPrice = new BigDecimal(p);
                }
            }
        }
        return info;
    }

    private static boolean planExists(Connection pg, String planId) throws SQLException {
        try (PreparedStatement ps = pg.prepareStatement("SELECT 1 FROM subscription_plans WHERE id=? AND status='ACTIVE'")) {
            ps.setString(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static long flush(PreparedStatement ins, Connection pg) throws SQLException {
        int[] counts = ins.executeBatch(); pg.commit();
        long ok = 0; for (int c : counts) if (c > 0 || c == Statement.SUCCESS_NO_INFO) ok++;
        System.out.printf("[CdkCodesMigrator] batch committed, inserted=%d\n", ok);
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Short) return ((Short) o).intValue();
        if (o instanceof Byte) return ((Byte) o).intValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0; // MySQL tinyint(1) → boolean
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception ignore) { return null; }
    }
}
