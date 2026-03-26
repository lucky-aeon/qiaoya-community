package org.xhy.community.application.skill.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.skill.dto.SkillDetailDTO;
import org.xhy.community.application.skill.dto.SkillListDTO;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;
import org.xhy.community.interfaces.skill.request.UpdateSkillRequest;

public class SkillAssembler {

    public static SkillEntity fromCreateRequest(CreateSkillRequest request, String userId) {
        return new SkillEntity(
                userId,
                request.getName(),
                request.getSummary(),
                request.getDescription(),
                request.getGithubUrl()
        );
    }

    public static SkillEntity fromUpdateRequest(UpdateSkillRequest request) {
        SkillEntity entity = new SkillEntity();
        entity.setName(request.getName());
        entity.setSummary(request.getSummary());
        entity.setDescription(request.getDescription());
        entity.setGithubUrl(request.getGithubUrl());
        return entity;
    }

    public static SkillQuery fromQueryRequest(SkillQueryRequest request) {
        SkillQuery query = new SkillQuery(request.getPageNum(), request.getPageSize());
        query.setKeyword(request.getKeyword());
        return query;
    }

    public static SkillQuery fromUserQueryRequest(SkillQueryRequest request, String userId) {
        SkillQuery query = fromQueryRequest(request);
        query.setUserId(userId);
        return query;
    }

    public static SkillListDTO toListDTO(SkillEntity entity, UserEntity author) {
        return toListDTO(entity, author, null, null, null);
    }

    public static SkillListDTO toListDTO(SkillEntity entity, UserEntity author,
                                         Long likeCount, Long favoriteCount, Long commentCount) {
        if (entity == null) {
            return null;
        }

        SkillListDTO dto = new SkillListDTO();
        BeanUtils.copyProperties(entity, dto);
        if (author != null) {
            dto.setAuthorName(author.getName());
        }
        dto.setLikeCount(normalizeCount(likeCount));
        dto.setFavoriteCount(normalizeCount(favoriteCount));
        dto.setCommentCount(normalizeCount(commentCount));
        return dto;
    }

    public static SkillDetailDTO toDetailDTO(SkillEntity entity, UserEntity author) {
        return toDetailDTO(entity, author, null, null, null);
    }

    public static SkillDetailDTO toDetailDTO(SkillEntity entity, UserEntity author,
                                             Long likeCount, Long favoriteCount, Long commentCount) {
        if (entity == null) {
            return null;
        }

        SkillDetailDTO dto = new SkillDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        if (author != null) {
            dto.setAuthorName(author.getName());
        }
        dto.setLikeCount(normalizeCount(likeCount));
        dto.setFavoriteCount(normalizeCount(favoriteCount));
        dto.setCommentCount(normalizeCount(commentCount));
        return dto;
    }

    private static Long normalizeCount(Long count) {
        return count == null ? 0L : count;
    }
}
