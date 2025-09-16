package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.AdminPostDTO;
import org.xhy.community.domain.post.entity.PostEntity;

import java.util.List;
import java.util.Map;

/**
 * 管理员文章转换器
 * 负责 PostEntity 与 AdminPostDTO 之间的转换
 */
public class AdminPostAssembler {
    
    /**
     * 将PostEntity转换为AdminPostDTO，包含作者名称和分类名称
     * 
     * @param post PostEntity实体
     * @param authorNames 作者ID到作者名称的映射
     * @param categoryNames 分类ID到分类名称的映射
     * @return AdminPostDTO
     */
    public static AdminPostDTO toDTO(PostEntity post, Map<String, String> authorNames, Map<String, String> categoryNames) {
        if (post == null) {
            return null;
        }
        
        AdminPostDTO dto = new AdminPostDTO();
        BeanUtils.copyProperties(post, dto);
        
        // 设置作者名称和分类名称
        dto.setAuthorName(authorNames.get(post.getAuthorId()));
        dto.setCategoryName(categoryNames.get(post.getCategoryId()));
        
        return dto;
    }
    
    /**
     * 批量转换PostEntity列表为AdminPostDTO列表
     * 
     * @param posts PostEntity列表
     * @param authorNames 作者ID到作者名称的映射
     * @param categoryNames 分类ID到分类名称的映射
     * @return AdminPostDTO列表
     */
    public static List<AdminPostDTO> toDTOList(List<PostEntity> posts, Map<String, String> authorNames, Map<String, String> categoryNames) {
        if (posts == null) {
            return null;
        }
        
        return posts.stream()
                .map(post -> toDTO(post, authorNames, categoryNames))
                .toList();
    }
}