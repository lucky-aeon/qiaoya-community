package org.xhy.community.domain.resourcebinding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.resourcebinding.entity.ResourceBindingEntity;
import org.xhy.community.domain.resourcebinding.repository.ResourceBindingRepository;
import org.xhy.community.domain.resourcebinding.valueobject.ResourceTargetType;
import org.xhy.community.infrastructure.markdown.MarkdownParser;
import org.xhy.community.infrastructure.markdown.model.MarkdownNode;
import org.xhy.community.infrastructure.markdown.model.NodeType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ResourceBindingDomainService {

    private final ResourceBindingRepository resourceBindingRepository;
    private final MarkdownParser markdownParser;

    public ResourceBindingDomainService(ResourceBindingRepository resourceBindingRepository,
                                        MarkdownParser markdownParser) {
        this.resourceBindingRepository = resourceBindingRepository;
        this.markdownParser = markdownParser;
    }

    /**
     * 按章节全量同步资源绑定（先清后建）
     */
    public void syncBindingsForChapter(String chapterId, Set<String> resourceIds) {
        // 逻辑删除原有绑定
        LambdaQueryWrapper<ResourceBindingEntity> deleteWrapper = new LambdaQueryWrapper<ResourceBindingEntity>()
                .eq(ResourceBindingEntity::getTargetType, ResourceTargetType.CHAPTER)
                .eq(ResourceBindingEntity::getTargetId, chapterId);
        resourceBindingRepository.delete(deleteWrapper);

        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }

        java.util.List<ResourceBindingEntity> list = new java.util.ArrayList<>(resourceIds.size());
        for (String rid : resourceIds) {
            list.add(new ResourceBindingEntity(rid, ResourceTargetType.CHAPTER, chapterId));
        }
        resourceBindingRepository.insert(list);
    }

    /**
     * 从 Markdown 文本解析资源 ID，并同步章节绑定
     * - 解析只做技术结构（由 MarkdownParser 完成），业务语义解析（提取资源ID）在本 Domain 内完成
     */
    public void syncBindingsForChapterFromMarkdown(String chapterId, String markdownContent) {
        MarkdownNode root = markdownParser.parse(markdownContent);
        Set<String> ids = extractResourceIds(root);
        syncBindingsForChapter(chapterId, ids);
    }

    /**
     * 业务规则：遍历 Node 树，提取 /api/public/resource/{id}/... 中的 id
     */
    public Set<String> extractResourceIds(MarkdownNode root) {
        Set<String> ids = new HashSet<>();
        if (root == null) return ids;
        visit(root, ids);
        return ids;
    }

    private void visit(MarkdownNode node, Set<String> out) {
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
            for (MarkdownNode c : node.getChildren()) {
                visit(c, out);
            }
        }
    }

    private String extractIdFromUrl(String url) {
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

    /**
     * 根据资源ID获取绑定记录
     */
    public List<ResourceBindingEntity> getBindingsByResourceId(String resourceId) {
        LambdaQueryWrapper<ResourceBindingEntity> query = new LambdaQueryWrapper<ResourceBindingEntity>()
                .eq(ResourceBindingEntity::getResourceId, resourceId);
        return resourceBindingRepository.selectList(query);
    }

    /**
     * 根据资源ID批量查询绑定记录
     */
    public List<ResourceBindingEntity> getBindingsByResourceIds(Collection<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<ResourceBindingEntity> query = new LambdaQueryWrapper<ResourceBindingEntity>()
                .in(ResourceBindingEntity::getResourceId, resourceIds);
        return resourceBindingRepository.selectList(query);
    }
}
