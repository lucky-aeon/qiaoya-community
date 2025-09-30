package org.xhy.community.tools.migration;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;

/**
 * 迁移订单：orders(MySQL) -> orders(PostgreSQL)
 * - amount(元) 原样写入 numeric
 * - order_type: acquisition_type 1=GIFT, 2=PURCHASE
 * - product 固定指向订阅套餐
 */
public class OrdersMigrator {

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

        System.out.println("[OrdersMigrator] MySQL → " + mysqlUrl);
        System.out.println("[OrdersMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            // 获取套餐名称
            String planName = readPlanName(pg, PLAN_ID);
            if (planName == null) throw new IllegalStateException("subscription_plans not found: " + PLAN_ID);

            pg.setAutoCommit(false);

            String selectSql = "SELECT id, invite_code, price, purchaser, acquisition_type, creator, created_at FROM orders ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String insertSql = "INSERT INTO orders (id, order_no, user_id, cdk_code, product_type, product_id, product_name, order_type, amount, activated_time, remark, create_time, update_time, extra, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insertSql)) {
                    long total = 0, inserted = 0; int batch = 0; final int batchSize = 1000;
                    Timestamp now = Timestamp.from(Instant.now());

                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            total++;
                            long id = rs.getLong("id");
                            String idStr = String.valueOf(id);
                            String code = rs.getString("invite_code");
                            String priceStr = rs.getString("price");
                            BigDecimal amount = priceStr == null ? BigDecimal.ZERO : new BigDecimal(priceStr);
                            int purchaser = rs.getInt("purchaser");
                            Integer acq = (Integer) rs.getObject("acquisition_type");
                            Integer creator = (Integer) rs.getObject("creator");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            if (createdAt == null) createdAt = now;

                            String orderNo = "LEGACY-" + idStr;
                            String orderType = (acq != null && acq == 1) ? "GIFT" : "PURCHASE"; // 1=赠送 2=购买

                            int i = 1;
                            ins.setString(i++, idStr);
                            ins.setString(i++, orderNo);
                            ins.setString(i++, String.valueOf(purchaser));
                            ins.setString(i++, code);
                            ins.setString(i++, "SUBSCRIPTION_PLAN");
                            ins.setString(i++, PLAN_ID);
                            ins.setString(i++, planName);
                            ins.setString(i++, orderType);
                            ins.setBigDecimal(i++, amount);
                            ins.setTimestamp(i++, createdAt); // activated_time
                            ins.setString(i++, null); // remark
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, createdAt);
                            // extra: 记录旧行元数据
                            String extraJson = String.format("{\"creator\":%s,\"legacy_order_id\":%s,\"acq_type\":%s}",
                                    creator == null ? "null" : String.valueOf(creator), idStr, acq == null ? "null" : String.valueOf(acq));
                            ins.setObject(i++, extraJson, Types.OTHER);
                            ins.setNull(i++, Types.TIMESTAMP); // deleted_at

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch = 0; }
                        }
                    }

                    if (batch > 0) inserted += flush(ins, pg);
                    System.out.printf("[OrdersMigrator] Done. total=%d, inserted=%d\n", total, inserted);
                }
            }
        }
    }

    private static String readPlanName(Connection pg, String planId) throws SQLException {
        try (PreparedStatement ps = pg.prepareStatement("SELECT name FROM subscription_plans WHERE id=?")) {
            ps.setString(1, planId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    private static long flush(PreparedStatement ins, Connection pg) throws SQLException {
        int[] counts = ins.executeBatch(); pg.commit();
        long ok = 0; for (int c : counts) if (c > 0 || c == Statement.SUCCESS_NO_INFO) ok++;
        System.out.printf("[OrdersMigrator] batch committed, inserted=%d\n", ok);
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
