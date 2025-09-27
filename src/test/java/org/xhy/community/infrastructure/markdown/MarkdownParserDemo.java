package org.xhy.community.infrastructure.markdown;

import org.xhy.community.infrastructure.markdown.impl.FlexmarkMarkdownParser;
import org.xhy.community.infrastructure.markdown.model.MarkdownNode;
import org.xhy.community.infrastructure.markdown.model.NodeType;

import java.util.HashSet;
import java.util.Set;

/**
 * 简易 Main 演示：输入一段 Markdown，经 MarkdownParser 解析为 Node 树，
 * 再在演示代码中按业务规则提取资源ID（/api/public/resource/{id}/...）。
 *
 * 注意：提取业务ID应在 Domain 层完成，这里仅为演示与人工验证。
 */
public class MarkdownParserDemo {

    public static void main(String[] args) {
        String markdown = String.join("\n",
                "# Markdown 解析与资源ID提取 Demo",
                "",
                "这是图片：",
                "![image#S #R #100% #auto](/api/public/resource/6e0fb19f69663da216606923f55af750/access)",
                "",
                "这是视频：",
                "!video[20250909220353-xhy的快速会议-视频-1.mp4#100% #auto](/api/public/resource/c292ae1a5a5b8e0639a2cf7f856cbaa1/access){poster=/api/public/resource/0de3ec4c42ef0a79a30ee80da2e3651d/access}",
                "",
                "绝对路径也应被识别：",
                "![abs](https://qiaoya.com/api/public/resource/AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE/access?x=1)"
        );

        MarkdownParser parser = new FlexmarkMarkdownParser();
        MarkdownNode root = parser.parse(markdown);

        Set<String> ids = extractResourceIds(root);

        System.out.println("Input markdown:\n" + markdown + "\n");
        System.out.println("Extracted resource IDs (" + ids.size() + "):");
        for (String id : ids) {
            System.out.println(" - " + id);
        }
    }

    // 演示用途：遍历 Node 树并提取资源ID
    static Set<String> extractResourceIds(MarkdownNode root) {
        Set<String> out = new HashSet<>();
        visit(root, out);
        return out;
    }

    static void visit(MarkdownNode node, Set<String> out) {
        if (node == null) return;
        if (node.getType() == NodeType.IMAGE) {
            String src = node.getAttributes().get("src");
            String id = extractIdFromUrl(src);
            if (id != null) out.add(id);
        } else if (node.getType() == NodeType.VIDEO) {
            String src = node.getAttributes().get("src");
            String poster = node.getAttributes().get("poster");
            String id1 = extractIdFromUrl(src);
            String id2 = extractIdFromUrl(poster);
            if (id1 != null) out.add(id1);
            if (id2 != null) out.add(id2);
        }
        if (node.getChildren() != null) {
            for (MarkdownNode c : node.getChildren()) visit(c, out);
        }
    }

    static String extractIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        int idx = url.indexOf("/api/public/resource/");
        if (idx < 0) return null;
        int start = idx + "/api/public/resource/".length();
        int end = url.indexOf('/', start);
        if (end < 0) return null;
        String id = url.substring(start, end);
        if (id.matches("[A-Fa-f0-9]{32}") || id.matches("[A-Fa-f0-9\\-]{36}")) {
            return id;
        }
        return null;
    }
}

