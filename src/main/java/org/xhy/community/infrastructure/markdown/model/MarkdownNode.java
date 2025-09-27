package org.xhy.community.infrastructure.markdown.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 技术中立的 Markdown Node 模型
 */
public class MarkdownNode {
    private final NodeType type;
    private final String text; // 可为 null
    private final Map<String, String> attributes; // src/href/lang/level 等
    private final List<MarkdownNode> children;

    private MarkdownNode(NodeType type, String text, Map<String, String> attributes, List<MarkdownNode> children) {
        this.type = type;
        this.text = text;
        this.attributes = attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        this.children = children == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(children));
    }

    public static Builder builder(NodeType type) {
        return new Builder(type);
    }

    public NodeType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<MarkdownNode> getChildren() {
        return children;
    }

    public static class Builder {
        private final NodeType type;
        private String text;
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<MarkdownNode> children = new ArrayList<>();

        public Builder(NodeType type) {
            this.type = type;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder attr(String key, String value) {
            if (key != null && value != null) {
                attributes.put(key, value);
            }
            return this;
        }

        public Builder child(MarkdownNode node) {
            if (node != null) {
                children.add(node);
            }
            return this;
        }

        public Builder children(List<MarkdownNode> nodes) {
            if (nodes != null) {
                for (MarkdownNode n : nodes) {
                    if (n != null) children.add(n);
                }
            }
            return this;
        }

        public MarkdownNode build() {
            return new MarkdownNode(type, text, attributes, children);
        }
    }
}

