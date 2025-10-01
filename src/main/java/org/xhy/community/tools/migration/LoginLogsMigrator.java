package org.xhy.community.tools.migration;

import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;

/**
 * 旧库 MySQL login_logs -> 新库 PostgreSQL user_activity_logs（仅认证相关）
 * - 通过新库 users(email) 反查 user_id（找不到则置 null）
 * - activity_type 映射：
 *   登录成功 -> LOGIN_SUCCESS
 *   登录失败/账号不存在/密码错误/账号或密码错误/操作次数过多 -> LOGIN_FAILED（failure_reason=state）
 *   注册成功 -> REGISTER_SUCCESS
 *   注册失败/表单校验失败 -> REGISTER_FAILED（failure_reason=state）
 * - request_path: 登录类 /api/auth/login；注册类 /api/auth/register；request_method 均置 POST
 * - equipment 去引号；context_data 保存 account 与 state_raw
 * - DRY_RUN=true 环境变量开启预览，不写库
 */
public class LoginLogsMigrator {

    public static void main(String[] args) throws Exception {
        boolean dryRun = Boolean.parseBoolean(env("DRY_RUN", "false"));
        int batchSize = parseInt(env("BATCH_SIZE", "1000"), 1000);

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

        System.out.println("[LoginLogsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[LoginLogsMigrator] Postgres -> " + pgUrl + " dryRun=" + dryRun);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String select = "SELECT id, account, state, browser, equipment, ip, created_at FROM login_logs ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String lookupUser = "SELECT id FROM users WHERE lower(email)=lower(?) LIMIT 1";
                try (PreparedStatement findUser = pg.prepareStatement(lookupUser)) {

                    String insert = "INSERT INTO user_activity_logs (id, user_id, activity_type, browser, equipment, ip, user_agent, failure_reason, created_at, updated_at, target_type, target_id, request_method, request_path, execution_time_ms, session_id, context_data, deleted_at) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL) ON CONFLICT (id) DO NOTHING";
                    try (PreparedStatement ins = pg.prepareStatement(insert)) {
                        long total=0, insCount=0; int batch=0;
                        java.util.Map<String,String> userCache = new java.util.HashMap<>(4096);
                        long loginOk=0, loginFail=0, regOk=0, regFail=0, skip=0;
                        try (ResultSet rs = sel.executeQuery()) {
                            while (rs.next()) {
                                total++;
                                long oldId = rs.getLong(1);
                                String account = rs.getString(2);
                                String state = rs.getString(3);
                                String browser = rs.getString(4);
                                String equipment = trimQuotes(rs.getString(5));
                                String ip = rs.getString(6);
                                Timestamp createdAt = rs.getTimestamp(7);
                                if (createdAt == null) createdAt = Timestamp.from(Instant.now());

                                // 判定活动类型与路径
                                Mapping m = mapActivity(state);
                                if (m == null) { skip++; continue; }
                                if ("LOGIN_SUCCESS".equals(m.activityType)) loginOk++;
                                else if ("LOGIN_FAILED".equals(m.activityType)) loginFail++;
                                else if ("REGISTER_SUCCESS".equals(m.activityType)) regOk++;
                                else if ("REGISTER_FAILED".equals(m.activityType)) regFail++;

                                // 反查 user_id（email）
                                String userId = null;
                                if (account != null && account.contains("@")) {
                                    String key = account.trim().toLowerCase();
                                    userId = userCache.get(key);
                                    if (userId == null) {
                                        findUser.setString(1, key);
                                        try (ResultSet ur = findUser.executeQuery()) {
                                            if (ur.next()) userId = ur.getString(1);
                                        }
                                        if (userId != null) {
                                            if (userCache.size() < 100_000) userCache.put(key, userId);
                                        }
                                    }
                                }

                                if (!dryRun) {
                                    int i=1;
                                    ins.setString(i++, md5("login:"+oldId));
                                    if (userId != null) ins.setString(i++, userId); else ins.setNull(i++, Types.VARCHAR);
                                    ins.setString(i++, m.activityType);
                                    ins.setString(i++, browser);
                                    ins.setString(i++, equipment);
                                    ins.setString(i++, ip==null?"":ip);
                                    ins.setString(i++, null); // user_agent 旧表有限度，保持空或未来增强
                                    if (m.failureReason != null) ins.setString(i++, m.failureReason); else ins.setNull(i++, Types.VARCHAR);
                                    ins.setTimestamp(i++, createdAt);
                                    ins.setTimestamp(i++, createdAt);
                                    ins.setNull(i++, Types.VARCHAR); // target_type
                                    ins.setNull(i++, Types.VARCHAR); // target_id
                                    ins.setString(i++, "POST");
                                    ins.setString(i++, m.requestPath);
                                    ins.setNull(i++, Types.INTEGER);
                                    ins.setNull(i++, Types.VARCHAR); // session_id 暂无
                                    String ctx = jsonContext(account, state);
                                    ins.setObject(i++, ctx, Types.OTHER);

                                    ins.addBatch();
                                    if (++batch>=batchSize) { ins.executeBatch(); pg.commit(); batch=0; }
                                    insCount++;
                                }
                            }
                        }
                        if (!dryRun && batch>0) { ins.executeBatch(); pg.commit(); }
                        System.out.printf("[LoginLogsMigrator] total=%d, inserted=%d, loginOk=%d, loginFail=%d, regOk=%d, regFail=%d, skipped=%d\n",
                                total, insCount, loginOk, loginFail, regOk, regFail, skip);
                    }
                }
            }
        }
    }

    private static class Mapping { String activityType; String requestPath; String failureReason; }

    private static Mapping mapActivity(String state) {
        if (state == null) return null;
        String s = state.trim();
        Mapping m = new Mapping();
        if (s.contains("登录成功")) { m.activityType = "LOGIN_SUCCESS"; m.requestPath = "/api/auth/login"; return m; }
        if (s.contains("登录失败") || s.contains("账号不存在") || s.contains("密码错误") || s.contains("账号或密码错误") || s.contains("操作次数过多")) {
            m.activityType = "LOGIN_FAILED"; m.requestPath = "/api/auth/login"; m.failureReason = s; return m; }
        if (s.contains("注册成功")) { m.activityType = "REGISTER_SUCCESS"; m.requestPath = "/api/auth/register"; return m; }
        if (s.contains("注册") || s.contains("验证") || s.contains("昵称已存在") || s.contains("注册码不存在") || s.contains("验证码不存在")) {
            m.activityType = "REGISTER_FAILED"; m.requestPath = "/api/auth/register"; m.failureReason = s; return m; }
        return null; // 未识别，跳过
    }

    private static String trimQuotes(String v) { if (v==null) return null; String t=v.trim(); if (t.startsWith("\"")&&t.endsWith("\"")) return t.substring(1,t.length()-1); return t; }
    private static String jsonContext(String account, String state) {
        String a = account==null?"":account.replace("\\","\\\\").replace("\"","\\\"");
        String s = state==null?"":state.replace("\\","\\\\").replace("\"","\\\"");
        return "{\"account\":\""+a+"\",\"state_raw\":\""+s+"\"}";
    }
    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder(bytes.length*2);
            for (byte b: bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return Long.toHexString(Double.doubleToLongBits(Math.random())); }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
