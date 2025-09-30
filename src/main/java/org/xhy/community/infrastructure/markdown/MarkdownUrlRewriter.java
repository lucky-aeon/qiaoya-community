package org.xhy.community.infrastructure.markdown;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.function.UnaryOperator;

/**
 * 基于 flexmark 的 Markdown 链接重写工具：遍历 AST，针对 Image/Link 的 url 进行替换，然后格式化输出。
 * 注意：不在 AST 节点中的纯文本旧链接不处理（避免误伤），如需处理可在迁移脚本中二次正则兜底。
 */
public class MarkdownUrlRewriter {

    private final Parser parser;
    private final Formatter formatter;

    public MarkdownUrlRewriter() {
        MutableDataSet options = new MutableDataSet();
        this.parser = Parser.builder(options).build();
        this.formatter = Formatter.builder(options).build();
    }

    /**
     * 重写 markdown 中的链接与图片 url。
     * @param markdown 原始 markdown
     * @param urlMapper 入参为原 url，返回替换后的 url（若返回相同值则视为不替换）
     * @return 重写后的 markdown 文本
     */
    public String rewrite(String markdown, UnaryOperator<String> urlMapper) {
        if (markdown == null || markdown.isBlank()) return markdown;
        Node doc = parser.parse(markdown);
        NodeVisitor visitor = new NodeVisitor(
                new VisitHandler<>(Image.class, node -> rewrite(node, urlMapper)),
                new VisitHandler<>(Link.class, node -> rewrite(node, urlMapper))
        );
        visitor.visit(doc);
        return formatter.render(doc);
    }

    private void rewrite(Image node, UnaryOperator<String> fn) {
        String url = node.getUrl().toString();
        String mapped = fn.apply(url);
        if (!url.equals(mapped)) node.setUrl(BasedSequence.of(mapped));
    }

    private void rewrite(Link node, UnaryOperator<String> fn) {
        String url = node.getUrl().toString();
        String mapped = fn.apply(url);
        if (!url.equals(mapped)) node.setUrl(BasedSequence.of(mapped));
    }
}

