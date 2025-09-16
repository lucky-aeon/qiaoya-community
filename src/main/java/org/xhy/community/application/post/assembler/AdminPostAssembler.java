package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.AdminPostDTO;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.query.PostQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.interfaces.post.request.AdminPostQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * 管理员文章转换器
 * 负责 PostEntity 与 AdminPostDTO 之间的转换
 */
public class AdminPostAssembler {
    
    /**
     * 从Request转换为PostQuery
     * 
     * @param request 管理员文章查询请求
     * @return PostQuery对象
     */
    public static PostQuery fromRequest(AdminPostQueryRequest request) {
        PostQuery query = new PostQuery(request.getPageNum(), request.getPageSize());
        query.setAccessLevel(AccessLevel.ADMIN);
        // AdminPostQueryRequest目前没有其他查询条件，如果后续添加则在这里设置
        return query;
    }
    
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