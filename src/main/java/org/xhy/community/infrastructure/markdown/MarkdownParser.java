package org.xhy.community.infrastructure.markdown;

import org.xhy.community.infrastructure.markdown.model.MarkdownNode;

/**
 * Markdown 解析器抽象：输入 Markdown 字符串，输出与实现无关的技术语义 Node 树。
 *
 * - 不承载任何业务语义（如资源ID、用户ID等），仅描述结构与属性（src、href、lang 等）。
 * - 允许不同实现（flexmark、regex 等）无缝替换。
 */
public interface MarkdownParser {
    /**
     * 解析 Markdown 文本为统一的 Node 树
     * @param markdown markdown 文本，可为空或空串
     * @return 根节点（type=DOCUMENT），永不返回 null
     */
    MarkdownNode parse(String markdown);
}

