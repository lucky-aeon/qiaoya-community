package org.xhy.community.infrastructure.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 从 classpath 读取模板内容
 */
@Component
public class ClasspathTemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(ClasspathTemplateLoader.class);

    // 基础路径：src/main/resources/templates/email/outapp
    private final String basePath = "templates/email/outapp/";

    public String load(String fileName) {
        String path = basePath + fileName;
        ClassPathResource resource = new ClassPathResource(path);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取模板失败: {}", path, e);
            throw new RuntimeException("无法读取模板: " + path, e);
        }
    }
}

