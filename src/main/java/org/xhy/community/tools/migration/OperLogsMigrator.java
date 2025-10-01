package org.xhy.community.tools.migration;

import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;

/**
 * 旧库 MySQL oper_logs -> 新库 PostgreSQL user_activity_logs（浏览与下载）
 *
 * 覆盖规则：
 * - 文章详情：/community/articles/{id} → VIEW_POST，target=POST({id})
 * - 章节详情：/community/courses/section/{id} → VIEW_COURSE，target=CHAPTER({id})
 * - 课程详情：/community/courses/{id} → VIEW_COURSE，target=COURSE({id})
 *
 * 其他说明：
 * - user_id：直接透传旧库的 user_id（新库使用字符串id，与旧库一致）
 * - execution_time_ms：解析 exec_at（支持 µs/ms/s/数值），失败置空并记录 context_data.exec_at_raw
 * - 幂等：ON CONFLICT(id) DO NOTHING；id=md5("oper|oldId|createdAt|request_info")
 * - 支持 DRY_RUN 与 BATCH_SIZE
 */
public class OperLogsMigrator {

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

        System.out.println("[OperLogsMigrator] MySQL -> " + mysqlUrl);
        System.out.println("[OperLogsMigrator] Postgres -> " + pgUrl + " dryRun=" + dryRun);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String select = "SELECT id, request_method, request_info, user_id, ip, exec_at, created_at, platform, user_agent FROM oper_logs ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(select, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String insert = "INSERT INTO user_activity_logs (id, user_id, activity_type, browser, equipment, ip, user_agent, failure_reason, created_at, updated_at, target_type, target_id, request_method, request_path, execution_time_ms, session_id, context_data, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NULL) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insert)) {
                    long total=0, inserted=0, skippedUnsupported=0, viewPost=0, viewChapter=0, viewCourse=0; int batch=0;
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            total++;
                            long oldId = rs.getLong(1);
                            String requestMethod = rs.getString(2);
                            String requestInfo = rs.getString(3);
                            Object uidObj = rs.getObject(4);
                            String ip = rs.getString(5);
                            String execAt = rs.getString(6);
                            Timestamp createdAt = getLocalTimestamp(rs, 7);
                            String platform = trimQuotes(rs.getString(8));
                            String userAgent = rs.getString(9);

                            if (createdAt == null) createdAt = Timestamp.from(Instant.now());
                            String id = md5("oper|" + oldId + "|" + createdAt.getTime() + "|" + (requestInfo==null?"":requestInfo));
                            Integer execMs = parseExecAt(execAt);
                            String pgUserId = null;
                            if (uidObj != null) {
                                if (uidObj instanceof Number) {
                                    pgUserId = String.valueOf(((Number) uidObj).longValue());
                                } else {
                                    pgUserId = String.valueOf(uidObj);
                                }
                            }

                            // 仅处理文章/章节/课程浏览，不处理下载

                            // 规则2：文章详情
                            String articleId = extractByPrefix(requestInfo, "/community/articles/");
                            if (articleId != null && articleId.matches("^\\d+$")) {
                                int i=1;
                                ins.setString(i++, id);
                                if (pgUserId == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, pgUserId);
                                ins.setString(i++, "VIEW_POST");
                                ins.setNull(i++, Types.VARCHAR);
                                if (platform == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, platform);
                                if (ip == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, ip);
                                if (userAgent == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, userAgent);
                                ins.setNull(i++, Types.VARCHAR);
                                ins.setTimestamp(i++, createdAt);
                                ins.setTimestamp(i++, createdAt);
                                ins.setString(i++, "POST");
                                ins.setString(i++, articleId);
                                if (requestMethod == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, requestMethod);
                                ins.setString(i++, requestInfo == null ? "/community/articles/"+articleId : requestInfo);
                                if (execMs == null) ins.setNull(i++, Types.INTEGER); else ins.setInt(i++, execMs);
                                ins.setNull(i++, Types.VARCHAR);
                                ins.setObject(i++, jsonContext("request_info", requestInfo), Types.OTHER);

                                if (!dryRun) ins.addBatch();
                                batch++;
                                if (!dryRun && batch >= batchSize) { ins.executeBatch(); pg.commit(); batch = 0; }
                                inserted++; viewPost++;
                                continue;
                            }

                            // 规则3：章节详情
                            String chapterId = extractByPrefix(requestInfo, "/community/courses/section/");
                            if (chapterId != null && chapterId.matches("^\\d+$")) {
                                int i=1;
                                ins.setString(i++, id);
                                if (pgUserId == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, pgUserId);
                                ins.setString(i++, "VIEW_COURSE"); // 或新增 VIEW_CHAPTER
                                ins.setNull(i++, Types.VARCHAR);
                                if (platform == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, platform);
                                if (ip == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, ip);
                                if (userAgent == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, userAgent);
                                ins.setNull(i++, Types.VARCHAR);
                                ins.setTimestamp(i++, createdAt);
                                ins.setTimestamp(i++, createdAt);
                                ins.setString(i++, "CHAPTER");
                                ins.setString(i++, chapterId);
                                if (requestMethod == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, requestMethod);
                                ins.setString(i++, requestInfo == null ? "/community/courses/section/"+chapterId : requestInfo);
                                if (execMs == null) ins.setNull(i++, Types.INTEGER); else ins.setInt(i++, execMs);
                                ins.setNull(i++, Types.VARCHAR);
                                ins.setObject(i++, jsonContext("request_info", requestInfo), Types.OTHER);

                                if (!dryRun) ins.addBatch();
                                batch++;
                                if (!dryRun && batch >= batchSize) { ins.executeBatch(); pg.commit(); batch = 0; }
                                inserted++; viewChapter++;
                                continue;
                            }

                            // 规则4：课程详情
                            if (requestInfo != null && requestInfo.startsWith("/community/courses/") && !requestInfo.startsWith("/community/courses/section/")) {
                                String courseId = extractByPrefix(requestInfo, "/community/courses/");
                                if (courseId != null && courseId.matches("^\\d+$")) {
                                    int i=1;
                                    ins.setString(i++, id);
                                    if (pgUserId == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, pgUserId);
                                    ins.setString(i++, "VIEW_COURSE");
                                    ins.setNull(i++, Types.VARCHAR);
                                    if (platform == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, platform);
                                    if (ip == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, ip);
                                    if (userAgent == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, userAgent);
                                    ins.setNull(i++, Types.VARCHAR);
                                    ins.setTimestamp(i++, createdAt);
                                    ins.setTimestamp(i++, createdAt);
                                    ins.setString(i++, "COURSE");
                                    ins.setString(i++, courseId);
                                    if (requestMethod == null) ins.setNull(i++, Types.VARCHAR); else ins.setString(i++, requestMethod);
                                    ins.setString(i++, requestInfo);
                                    if (execMs == null) ins.setNull(i++, Types.INTEGER); else ins.setInt(i++, execMs);
                                    ins.setNull(i++, Types.VARCHAR);
                                    ins.setObject(i++, jsonContext("request_info", requestInfo), Types.OTHER);

                                    if (!dryRun) ins.addBatch();
                                    batch++;
                                    if (!dryRun && batch >= batchSize) { ins.executeBatch(); pg.commit(); batch = 0; }
                                    inserted++; viewCourse++;
                                    continue;
                                }
                            }

                            // 其他：跳过
                            skippedUnsupported++;
                        }
                    }

                    if (!dryRun && batch > 0) { ins.executeBatch(); pg.commit(); }
                    System.out.printf("[OperLogsMigrator] total=%d, inserted=%d, viewPost=%d, viewChapter=%d, viewCourse=%d, skippedUnsupported=%d\n",
                            total, inserted, viewPost, viewChapter, viewCourse, skippedUnsupported);
                }
            }
        }
    }

    // 工具方法
    private static String trimQuotes(String v) { if (v==null) return null; String t=v.trim(); if (t.startsWith("\"")&&t.endsWith("\"")) return t.substring(1,t.length()-1); return t; }
    private static String escape(String v) { return v==null?"":v.replace("\\","\\\\").replace("\"","\\\""); }
    private static String jsonContext(String k, String v) { return "{\""+escape(k)+"\":\""+escape(v)+"\"}"; }
    private static String extractByPrefix(String p, String prefix) { if (p==null) return null; String s=p.trim(); if (!s.startsWith(prefix)) return null; String tail=s.substring(prefix.length()); int q=tail.indexOf('?'); if (q>=0) tail=tail.substring(0,q); if (tail.endsWith("/")) tail=tail.substring(0,tail.length()-1); return tail; }
    private static Integer parseExecAt(String s) {
        if (s == null || s.isEmpty()) return null; String t=s.trim().toLowerCase();
        try {
            if (t.endsWith("µs") || t.endsWith("μs")) { double us = Double.parseDouble(t.substring(0, t.length()-2).trim()); return (int)Math.round(us/1000.0); }
            if (t.endsWith("ms")) { double ms = Double.parseDouble(t.substring(0, t.length()-2).trim()); return (int)Math.round(ms); }
            if (t.endsWith("s")) { double sec = Double.parseDouble(t.substring(0, t.length()-1).trim()); return (int)Math.round(sec*1000); }
            if (t.matches("^-?\\d+(\\.\\d+)?$")) { double val = Double.parseDouble(t); return (int)Math.round(val); }
        } catch (Exception ignored) { }
        return null;
    }
    private static Timestamp getLocalTimestamp(ResultSet rs, int idx) throws SQLException {
        try {
            java.time.LocalDateTime ldt = rs.getObject(idx, java.time.LocalDateTime.class);
            if (ldt != null) return Timestamp.valueOf(ldt);
        } catch (Throwable ignore) { }
        return rs.getTimestamp(idx);
    }
    private static String env(String key, String def) { String v = System.getenv(key); if (v == null || v.isEmpty()) v = System.getProperty(key); return (v == null || v.isEmpty()) ? def : v; }
    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private static String md5(String s) { try { MessageDigest md = MessageDigest.getInstance("MD5"); byte[] b = md.digest(s.getBytes()); StringBuilder sb=new StringBuilder(b.length*2); for (byte x: b) sb.append(String.format("%02x", x)); return sb.toString(); } catch (Exception e) { return Long.toHexString(Double.doubleToLongBits(Math.random())); } }
}
