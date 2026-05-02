package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.markdown.MarkdownParser;
import org.xhy.community.infrastructure.markdown.model.MarkdownNode;
import org.xhy.community.infrastructure.markdown.model.NodeType;

import java.util.Optional;

@Service
public class ChapterVideoResourceResolver {

    private static final String RESOURCE_URL_PREFIX = "/api/public/resource/";

    private final MarkdownParser markdownParser;

    public ChapterVideoResourceResolver(MarkdownParser markdownParser) {
        this.markdownParser = markdownParser;
    }

    public Optional<String> resolveFirstVideoResourceId(String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) {
            return Optional.empty();
        }
        try {
            MarkdownNode root = markdownParser.parse(markdownContent);
            return findVideoResource(root);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<String> findVideoResource(MarkdownNode node) {
        if (node == null) {
            return Optional.empty();
        }
        if (node.getType() == NodeType.VIDEO) {
            Optional<String> fromSrc = extractResourceId(node.getAttributes().get("src"));
            if (fromSrc.isPresent()) {
                return fromSrc;
            }
        }
        if (node.getChildren() != null) {
            for (MarkdownNode child : node.getChildren()) {
                Optional<String> found = findVideoResource(child);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractResourceId(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        int prefixIndex = url.indexOf(RESOURCE_URL_PREFIX);
        if (prefixIndex < 0) {
            return Optional.empty();
        }
        int idStart = prefixIndex + RESOURCE_URL_PREFIX.length();
        int idEnd = url.indexOf('/', idStart);
        if (idEnd < 0) {
            return Optional.empty();
        }
        String id = url.substring(idStart, idEnd);
        if (id.isBlank() || !url.substring(idEnd).startsWith("/access") || !isResourceIdSegment(id)) {
            return Optional.empty();
        }
        return Optional.of(id);
    }

    private boolean isResourceIdSegment(String id) {
        for (int i = 0; i < id.length(); i++) {
            char ch = id.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_') {
                return false;
            }
        }
        return true;
    }
}
