package org.xhy.community.tools.migration;

import java.sql.*;
import java.time.Instant;

/**
 * 将旧库 files 表导入到新库 resources 表（仅写 resources，不写 resource_bindings）。
 * - 资源ID：直接沿用旧 files.id 的字符串
 * - original_name：取 file_key 最后一段文件名（无则用 file_key）
 * - resource_type：根据 format 或文件后缀简单判定 IMAGE/VIDEO/AUDIO/DOCUMENT/OTHER
 * - user_id：沿用旧 files.user_id（为空则写 "0"）
 */
public class FilesToResourcesMigrator {

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

        System.out.println("[FilesToResourcesMigrator] MySQL → " + mysqlUrl);
        System.out.println("[FilesToResourcesMigrator] Postgres → " + pgUrl);

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {

            pg.setAutoCommit(false);

            String selectSql = "SELECT id, file_key, size, format, user_id, created_at, updated_at FROM files ORDER BY id";
            try (PreparedStatement sel = mysql.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sel.setFetchSize(1000);

                String insertSql = "INSERT INTO resources (id, file_key, size, format, user_id, resource_type, original_name, create_time, update_time, deleted_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,NULL) ON CONFLICT (id) DO NOTHING";
                try (PreparedStatement ins = pg.prepareStatement(insertSql)) {
                    int batch=0; final int batchSize=1000; long inserted=0; Timestamp now = Timestamp.from(Instant.now());
                    try (ResultSet rs = sel.executeQuery()) {
                        while (rs.next()) {
                            String id = String.valueOf(rs.getInt("id"));
                            String fileKey = rs.getString("file_key");
                            long size = 0L; Object sizeObj = rs.getObject("size"); if (sizeObj instanceof Number) size = ((Number) sizeObj).longValue();
                            String format = rs.getString("format");
                            String userId = String.valueOf(rs.getInt("user_id"));
                            if (userId == null || userId.equals("0")) userId = "0";
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = createdAt;

                            String originalName = deriveOriginalName(fileKey, format);
                            String resourceType = classifyType(format, originalName);

                            int i=1;
                            ins.setString(i++, id);
                            ins.setString(i++, fileKey);
                            ins.setLong(i++, size);
                            ins.setString(i++, format);
                            ins.setString(i++, userId);
                            ins.setString(i++, resourceType);
                            ins.setString(i++, originalName);
                            ins.setTimestamp(i++, createdAt);
                            ins.setTimestamp(i++, updatedAt);
                            ins.addBatch();
                            if (++batch >= batchSize) { inserted += flush(ins, pg); batch=0; }
                        }
                    }
                    if (batch > 0) inserted += flush(ins, pg);
                    System.out.printf("[FilesToResourcesMigrator] inserted=%d\n", inserted);
                }
            }
        }
    }

    private static String deriveOriginalName(String fileKey, String format) {
        if (fileKey == null || fileKey.isBlank()) return "legacy";
        int slash = fileKey.lastIndexOf('/');
        String name = slash >=0 ? fileKey.substring(slash+1) : fileKey;
        if (name.isEmpty()) name = "legacy";
        return name;
    }

    private static String classifyType(String format, String name) {
        String ext = null;
        if (format != null && format.contains("/")) {
            String[] parts = format.split("/");
            ext = parts[1];
        } else if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.')+1);
        }
        if (ext == null) return "OTHER";
        String e = ext.toLowerCase();
        if (e.matches("jpg|jpeg|png|gif|bmp|webp|svg")) return "IMAGE";
        if (e.matches("mp4|avi|mov|wmv|flv|webm|mkv")) return "VIDEO";
        if (e.matches("mp3|wav|flac|aac|ogg|wma")) return "AUDIO";
        if (e.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf")) return "DOCUMENT";
        return "OTHER";
    }

    private static long flush(PreparedStatement ins, Connection pg) throws SQLException {
        int[] counts = ins.executeBatch();
        pg.commit();
        long ok=0; for (int c:counts) if (c>0 || c==Statement.SUCCESS_NO_INFO) ok++;
        return ok;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

