package org.xhy.community.infrastructure.markdown.impl;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.Set;

/**
 * flexmark 的内联解析扩展，支持 !video[alt](src){poster=...}
 */
public class VideoExtension implements Parser.ParserExtension {

    private VideoExtension() {}

    public static VideoExtension create() { return new VideoExtension(); }

    @Override
    public void extend(Parser.Builder builder) {
        builder.customInlineParserExtensionFactory(new Factory());
    }

    @Override
    public void parserOptions(MutableDataHolder options) {
        // no-op
    }

    static class Factory implements InlineParserExtensionFactory {
        @Override
        public InlineParserExtension apply(LightInlineParser lightInlineParser) {
            return new VideoInlineParser(lightInlineParser);
        }

        @Override
        public CharSequence getCharacters() {
            return "!"; // 仅当遇到 '!' 时触发
        }

        @Override
        public Set<Class<?>> getAfterDependents() { return null; }

        @Override
        public Set<Class<?>> getBeforeDependents() { return null; }

        @Override
        public boolean affectsGlobalScope() { return false; }
    }

    static class VideoInlineParser implements InlineParserExtension {
        private final LightInlineParser parser;

        VideoInlineParser(LightInlineParser parser) { this.parser = parser; }

        // Light API hooks（非接口必须，保留兼容，不标记@Override）
        public void finalizeDocument(LightInlineParser lightInlineParser) { }

        public void finalizeBlock(LightInlineParser lightInlineParser) { }

        @Override
        public boolean parse(LightInlineParser lip) {
            BasedSequence input = lip.getInput();
            int index = lip.getIndex();
            int len = input.length();

            // 必须以 !video[ 开头
            if (index + 7 > len) return false;
            if (!"!video[".contentEquals(input.subSequence(index, index + 7))) return false;

            int pos = index + 7; // after !video[

            // 读取 alt 到 ']'
            int endAlt = input.indexOf(']', pos);
            if (endAlt == -1) return false;

            // 期待 '(' url ')'
            if (endAlt + 1 >= len || input.charAt(endAlt + 1) != '(') return false;
            int startUrl = endAlt + 2;
            int endUrl = input.indexOf(')', startUrl);
            if (endUrl == -1) return false;
            String url = input.subSequence(startUrl, endUrl).toString();

            // 可选属性：{ poster=... }
            String poster = null;
            int next = endUrl + 1;
            if (next < len && input.charAt(next) == '{') {
                int endAttr = input.indexOf('}', next + 1);
                if (endAttr != -1) {
                    String attrs = input.subSequence(next + 1, endAttr).toString();
                    for (String part : attrs.split("\\s+")) {
                        if (part.startsWith("poster=")) {
                            poster = part.substring("poster=".length());
                            break;
                        }
                    }
                    next = endAttr + 1;
                }
            }

            int end = next;

            Node node = new VideoNode(url, poster);
            lip.appendNode(node);
            lip.setIndex(end);
            return true;
        }

        // Implement required finalize signatures for current flexmark interface
        @Override
        public void finalizeDocument(InlineParser inlineParser) { }

        @Override
        public void finalizeBlock(InlineParser inlineParser) { }
    }
}

