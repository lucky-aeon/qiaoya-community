package org.xhy.community.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

/**
 * 邮件品牌配置（Logo、设置链接等）
 */
@Configuration
@ConfigurationProperties(prefix = "community.email.brand")
public class EmailBrandingConfig {

    /** logo 的 data URI 或 URL */
    private String logoSrc;
    /** 可选：logo 文件路径；当 logoSrc 为空时使用该文件生成 data URI */
    private String logoFile;
    /** 通知设置链接 */
    private String manageNotificationsUrl = "https://qiaoya.com/settings/notifications";

    public String getLogoSrc() {
        if (StringUtils.hasText(logoSrc)) {
            return logoSrc;
        }
        // 优先从 logoFile 读取
        if (StringUtils.hasText(logoFile)) {
            // 支持 classpath: 前缀
            if (logoFile.startsWith("classpath:")) {
                String cp = logoFile.substring("classpath:".length());
                try {
                    ClassPathResource r = new ClassPathResource(cp);
                    byte[] bytes = r.getInputStream().readAllBytes();
                    String mime = guessMime(cp);
                    String b64 = Base64.getEncoder().encodeToString(bytes);
                    return "data:" + mime + ";base64," + b64;
                } catch (IOException ignored) {}
            } else {
                try {
                    byte[] bytes = Files.readAllBytes(Path.of(logoFile));
                    String mime = guessMime(logoFile);
                    String b64 = Base64.getEncoder().encodeToString(bytes);
                    return "data:" + mime + ";base64," + b64;
                } catch (IOException ignored) {}
            }
        }
        // 默认尝试 classpath 默认位置（若存在）
        try {
            ClassPathResource r = new ClassPathResource("branding/logo-email.jpg");
            if (r.exists()) {
                byte[] bytes = r.getInputStream().readAllBytes();
                String b64 = Base64.getEncoder().encodeToString(bytes);
                return "data:image/jpeg;base64," + b64;
            }
        } catch (IOException ignored) {}
        return null;
    }

    public void setLogoSrc(String logoSrc) { this.logoSrc = logoSrc; }
    public String getLogoFile() { return logoFile; }
    public void setLogoFile(String logoFile) { this.logoFile = logoFile; }
    public String getManageNotificationsUrl() { return manageNotificationsUrl; }
    public void setManageNotificationsUrl(String manageNotificationsUrl) { this.manageNotificationsUrl = manageNotificationsUrl; }

    private String guessMime(String path) {
        String p = Objects.toString(path, "").toLowerCase(Locale.ROOT);
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        if (p.endsWith(".gif")) return "image/gif";
        if (p.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
