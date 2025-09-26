package org.xhy.community.infrastructure.template;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 简单占位符渲染器：将 {{KEY}} 替换为对应值（不做转义）。
 * HTML 安全性由模板作者和调用方负责（当前用于邮件模板，已受控）。
 */
@Component
public class SimpleTemplateRenderer {

    public String render(String template, Map<String, String> context) {
        if (template == null || template.isEmpty() || context == null || context.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> e : context.entrySet()) {
            String key = e.getKey();
            String val = e.getValue() == null ? "" : e.getValue();
            result = result.replace("{{" + key + "}}", val);
        }
        return result;
    }
}

