package org.xhy.community.infrastructure.markdown.impl;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Component;
import org.xhy.community.infrastructure.markdown.MarkdownParser;
import org.xhy.community.infrastructure.markdown.model.MarkdownNode;
import org.xhy.community.infrastructure.markdown.model.NodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 flexmark 的 Markdown 解析实现，将 flexmark AST 映射为技术中立的 MarkdownNode 树。
 */
@Component
public class FlexmarkMarkdownParser implements MarkdownParser {

    private final Parser parser;

    public FlexmarkMarkdownParser() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
                AttributesExtension.create(),
                VideoExtension.create()
        ));
        this.parser = Parser.builder(options).build();
    }

    @Override
    public MarkdownNode parse(String markdown) {
        Document doc = parser.parse(markdown == null ? "" : markdown);
        return mapNode(doc);
    }

    private MarkdownNode mapNode(Node node) {
        if (node instanceof Document) {
            return MarkdownNode.builder(NodeType.DOCUMENT)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof Paragraph) {
            return MarkdownNode.builder(NodeType.PARAGRAPH)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof Heading) {
            Heading h = (Heading) node;
            return MarkdownNode.builder(NodeType.HEADING)
                    .attr("level", String.valueOf(h.getLevel()))
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof Image) {
            Image img = (Image) node;
            String url = img.getUrl().unescape();
            return MarkdownNode.builder(NodeType.IMAGE)
                    .attr("src", url)
                    .build();
        }
        if (node instanceof Link) {
            Link link = (Link) node;
            String href = link.getUrl().unescape();
            return MarkdownNode.builder(NodeType.LINK)
                    .attr("href", href)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof FencedCodeBlock) {
            FencedCodeBlock cb = (FencedCodeBlock) node;
            String lang = cb.getInfo().toString();
            String text = cb.getContentChars().toString();
            return MarkdownNode.builder(NodeType.CODE_BLOCK)
                    .attr("lang", lang)
                    .text(text)
                    .build();
        }
        if (node instanceof Code) {
            Code code = (Code) node;
            return MarkdownNode.builder(NodeType.INLINE_CODE)
                    .text(code.getText().toString())
                    .build();
        }
        if (node instanceof Emphasis) {
            return MarkdownNode.builder(NodeType.EMPHASIS)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof StrongEmphasis) {
            return MarkdownNode.builder(NodeType.STRONG)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof BulletList) {
            return MarkdownNode.builder(NodeType.LIST)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof ListItem) {
            return MarkdownNode.builder(NodeType.LIST_ITEM)
                    .children(mapChildren(node))
                    .build();
        }
        if (node instanceof com.vladsch.flexmark.ast.Text) {
            String txt = ((com.vladsch.flexmark.ast.Text) node).getChars().toString();
            return MarkdownNode.builder(NodeType.TEXT).text(txt).build();
        }
        if (node instanceof VideoNode) {
            VideoNode v = (VideoNode) node;
            return MarkdownNode.builder(NodeType.VIDEO)
                    .attr("src", v.getSrc())
                    .attr("poster", v.getPoster())
                    .build();
        }

        // 默认未知类型，继续映射子节点
        return MarkdownNode.builder(NodeType.UNKNOWN)
                .children(mapChildren(node))
                .build();
    }

    private List<MarkdownNode> mapChildren(Node node) {
        List<MarkdownNode> list = new ArrayList<>();
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            list.add(mapNode(child));
        }
        return list;
    }
}

