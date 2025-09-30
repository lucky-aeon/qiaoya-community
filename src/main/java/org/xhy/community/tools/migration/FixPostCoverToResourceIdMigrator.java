package org.xhy.community.tools.migration;

import java.sql.*;

/**
 * 将 posts.cover_image 统一修正为资源ID（而不是URL）。
 * 兼容以下三种输入：
 * 1) /api/public/resource/{id}/access -> 提取 {id}
 * 2) /api/community/file/singUrl?fileKey={fileKey} -> 通过 resources(file_key) 映射到 id
 * 3) 直接是 fileKey（如 13/xxxx） -> 通过 resources(file_key) 映射到 id
 * 若无法映射，则保持原值不动（便于人工排查）。
 */
public class FixPostCoverToResourceIdMigrator {

    public static void main(String[] args) throws Exception {
        String pgHost = env("TARGET_PG_HOST", "124.220.234.136");
        String pgPort = env("TARGET_PG_PORT", "5432");
        String pgDb = env("TARGET_PG_DB", "qiaoya_community");
        String pgUser = env("TARGET_PG_USER", env("DB_USERNAME", "qiaoya_community"));
        String pgPass = env("TARGET_PG_PASS", env("DB_PASSWORD", null));
        if (pgPass == null || pgPass.isEmpty()) throw new IllegalArgumentException("Missing TARGET_PG_PASS/DB_PASSWORD");

        String pgUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDb);
        System.out.println("[FixPostCoverToResourceIdMigrator] Postgres -> " + pgUrl);

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);

            String select = "SELECT id, cover_image FROM posts ORDER BY id";
            String update = "UPDATE posts SET cover_image=?, update_time=now() WHERE id=?";
            String mapByFileKey = "SELECT id FROM resources WHERE file_key=?";

            try (PreparedStatement sel = pg.prepareStatement(select);
                 PreparedStatement upd = pg.prepareStatement(update);
                 PreparedStatement map = pg.prepareStatement(mapByFileKey)) {

                int updated = 0, batch = 0, batchSize = 500;
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        String postId = rs.getString(1);
                        String cover = rs.getString(2);
                        if (cover == null || cover.isBlank()) continue;

                        String resourceId = null;

                        // 1) 已是新URL：/api/public/resource/{id}/access
                        String prefix = "/api/public/resource/";
                        int idx = cover.indexOf(prefix);
                        if (idx >= 0) {
                            int start = idx + prefix.length();
                            int end = cover.indexOf('/', start);
                            if (end > start) resourceId = cover.substring(start, end);
                        }

                        // 2) 旧URL：.../singUrl?fileKey=xxx
                        if (resourceId == null && cover.contains("singUrl?fileKey=")) {
                            String fk = cover.substring(cover.indexOf("singUrl?fileKey=") + "singUrl?fileKey=".length());
                            int cut = fk.indexOf('&'); if (cut > 0) fk = fk.substring(0, cut);
                            fk = java.net.URLDecoder.decode(fk, java.nio.charset.StandardCharsets.UTF_8);
                            map.setString(1, fk);
                            try (ResultSet mr = map.executeQuery()) { if (mr.next()) resourceId = mr.getString(1); }
                        }

                        // 3) 直接是 fileKey
                        if (resourceId == null && cover.matches("\\d+/[\\w\\-\\.]+")) {
                            map.setString(1, cover);
                            try (ResultSet mr = map.executeQuery()) { if (mr.next()) resourceId = mr.getString(1); }
                        }

                        if (resourceId != null && !resourceId.equals(cover)) {
                            upd.setString(1, resourceId);
                            upd.setString(2, postId);
                            upd.addBatch();
                            if (++batch >= batchSize) { upd.executeBatch(); batch = 0; }
                            updated++;
                        }
                    }
                }
                if (batch > 0) upd.executeBatch();
                pg.commit();
                System.out.println("[FixPostCoverToResourceIdMigrator] posts updated=" + updated);
            }
        }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isEmpty()) v = System.getProperty(key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}

