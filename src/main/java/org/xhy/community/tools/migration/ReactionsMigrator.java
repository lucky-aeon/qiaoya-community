package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;

/**
 * 旧库 MySQL reactions -> 新库 PostgreSQL reactions
 * 业务类型映射：0=POST，1=COMMENT，2=COURSE，其它（3=分享会，4=AI日报）跳过
 * reaction_type 原样（对应 expression_types.code）
 */
public class ReactionsMigrator {

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

        System.out.println("[ReactionsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[ReactionsMigrator] Postgres -> " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String select = "SELECT id, business_type, business_id, user_id, reaction_type, created_at, updated_at, deleted_at FROM reactions ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String insert = "INSERT INTO reactions (id, business_type, business_id, user_id, reaction_type, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    int batch=0, batchSize=1000; long inserted=0, skipped=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getLong(1));
                            Integer bt = toInt(rs.getObject(2));
                            String businessType = mapBusinessType(bt);
                            if (businessType == null) { skipped++; continue; }
                            String businessId = String.valueOf(rs.getLong(3));
                            String userId = String.valueOf(rs.getLong(4));
                            String reactionType = rs.getString(5);
                            Timestamp createdAt = rs.getTimestamp(6);
                            Timestamp updatedAt = rs.getTimestamp(7);
                            Timestamp deletedAt = rs.getTimestamp(8);
                            if (createdAt == null) createdAt = Timestamp.from(Instant.now());
                            if (updatedAt == null) updatedAt = createdAt;

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, businessType);
                            ins.setString(i++, businessId);
                            ins.setString(i++, userId);
                            ins.setString(i++, reactionType);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            if (deletedAt != null) ins.setTimestamp(i++, deletedAt); else ins.setNull(i++, Types.TIMESTAMP);

                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                    }
                    if (batch>0) inserted += flush(ins, pg);
                    System.out.println("[ReactionsMigrator] inserted="+inserted+", skipped="+skipped);
                }
            }
        }
    }

    private static String mapBusinessType(Integer bt) {
        if (bt == null) return null;
        switch (bt) {
            case 0: return "POST";
            case 1: return "COMMENT";
            case 2: return "COURSE";
            // 3=分享会, 4=AI日报 -> 新库未建对应业务，跳过
            default: return null;
        }
    }

    private static int flush(PreparedStatement ps, Connection pg) throws SQLException {
        int[] counts = ps.executeBatch(); pg.commit();
        int ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
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
