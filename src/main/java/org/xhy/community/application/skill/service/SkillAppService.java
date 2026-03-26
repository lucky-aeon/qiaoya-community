package org.xhy.community.application.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xhy.community.application.skill.assembler.SkillAssembler;
import org.xhy.community.application.skill.dto.SkillDetailDTO;
import org.xhy.community.application.skill.dto.SkillListDTO;
import org.xhy.community.application.skill.dto.SkillStatsDTO;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.favorite.service.FavoriteDomainService;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.skill.service.SkillDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;
import org.xhy.community.interfaces.skill.request.UpdateSkillRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SkillAppService {

    private final SkillDomainService skillDomainService;
    private final UserDomainService userDomainService;
    private final LikeDomainService likeDomainService;
    private final FavoriteDomainService favoriteDomainService;
    private final CommentDomainService commentDomainService;

    public SkillAppService(SkillDomainService skillDomainService,
                           UserDomainService userDomainService,
                           LikeDomainService likeDomainService,
                           FavoriteDomainService favoriteDomainService,
                           CommentDomainService commentDomainService) {
        this.skillDomainService = skillDomainService;
        this.userDomainService = userDomainService;
        this.likeDomainService = likeDomainService;
        this.favoriteDomainService = favoriteDomainService;
        this.commentDomainService = commentDomainService;
    }

    @Transactional
    public SkillDetailDTO createSkill(CreateSkillRequest request, String currentUserId) {
        validateGithubUrl(request.getGithubUrl());
        SkillEntity createdSkill = skillDomainService.createSkill(SkillAssembler.fromCreateRequest(request, currentUserId));
        UserEntity author = userDomainService.getUserById(createdSkill.getUserId());
        return buildDetailDTO(createdSkill, author);
    }

    @Transactional
    public SkillDetailDTO updateSkill(String skillId, UpdateSkillRequest request, String currentUserId) {
        validateGithubUrl(request.getGithubUrl());
        SkillEntity updatedSkill = skillDomainService.updateSkill(
                skillId,
                SkillAssembler.fromUpdateRequest(request),
                currentUserId
        );
        UserEntity author = userDomainService.getUserById(updatedSkill.getUserId());
        return buildDetailDTO(updatedSkill, author);
    }

    public SkillDetailDTO getUserSkillById(String skillId, String currentUserId) {
        SkillEntity skill = skillDomainService.getUserSkillById(skillId, currentUserId);
        UserEntity author = userDomainService.getUserById(skill.getUserId());
        return buildDetailDTO(skill, author);
    }

    public SkillDetailDTO getPublicSkillById(String skillId) {
        SkillEntity skill = skillDomainService.getSkillById(skillId);
        UserEntity author = userDomainService.getUserById(skill.getUserId());
        return buildDetailDTO(skill, author);
    }

    public IPage<SkillListDTO> queryMySkills(String currentUserId, SkillQueryRequest request) {
        SkillQuery query = SkillAssembler.fromUserQueryRequest(request, currentUserId);
        return buildListPage(skillDomainService.querySkills(query));
    }

    public IPage<SkillListDTO> queryPublicSkills(SkillQueryRequest request) {
        SkillQuery query = SkillAssembler.fromQueryRequest(request);
        return buildListPage(skillDomainService.querySkills(query));
    }

    @Transactional
    public void deleteSkill(String skillId, String currentUserId) {
        skillDomainService.deleteSkill(skillId, currentUserId);
    }

    public SkillStatsDTO getSkillStats() {
        return new SkillStatsDTO(skillDomainService.countSkills());
    }

    private IPage<SkillListDTO> buildListPage(IPage<SkillEntity> entityPage) {
        List<SkillEntity> records = entityPage.getRecords();
        Page<SkillListDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        if (records.isEmpty()) {
            dtoPage.setRecords(List.of());
            return dtoPage;
        }

        Set<String> userIds = records.stream()
                .map(SkillEntity::getUserId)
                .collect(Collectors.toSet());
        Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(userIds);
        Set<String> skillIds = records.stream()
                .map(SkillEntity::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Long> likeCountMap = likeDomainService.batchCountLikes(buildLikeTargetMap(skillIds));
        Map<String, Long> favoriteCountMap = favoriteDomainService.batchCountFavorites(buildFavoriteTargetMap(skillIds));
        Map<String, Long> commentCountMap = commentDomainService.getCommentCountMapByBusinessIds(skillIds, BusinessType.SKILL);

        List<SkillListDTO> dtoList = records.stream()
                .map(skill -> SkillAssembler.toListDTO(
                        skill,
                        userMap.get(skill.getUserId()),
                        likeCountMap.getOrDefault(buildTargetKey(LikeTargetType.SKILL.name(), skill.getId()), 0L),
                        favoriteCountMap.getOrDefault(buildTargetKey(FavoriteTargetType.SKILL.name(), skill.getId()), 0L),
                        commentCountMap.getOrDefault(skill.getId(), 0L)
                ))
                .toList();
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    private SkillDetailDTO buildDetailDTO(SkillEntity skill, UserEntity author) {
        long likeCount = likeDomainService.countLikes(skill.getId(), LikeTargetType.SKILL);
        long favoriteCount = favoriteDomainService.countFavorites(skill.getId(), FavoriteTargetType.SKILL);
        long commentCount = commentDomainService.getCommentCountByBusiness(skill.getId(), BusinessType.SKILL);
        return SkillAssembler.toDetailDTO(skill, author, likeCount, favoriteCount, commentCount);
    }

    private Map<String, LikeTargetType> buildLikeTargetMap(Set<String> skillIds) {
        Map<String, LikeTargetType> targets = new java.util.HashMap<>();
        skillIds.forEach(id -> targets.put(id, LikeTargetType.SKILL));
        return targets;
    }

    private Map<String, FavoriteTargetType> buildFavoriteTargetMap(Set<String> skillIds) {
        Map<String, FavoriteTargetType> targets = new java.util.HashMap<>();
        skillIds.forEach(id -> targets.put(id, FavoriteTargetType.SKILL));
        return targets;
    }

    private String buildTargetKey(String targetTypeName, String targetId) {
        return targetTypeName + ":" + targetId;
    }

    private void validateGithubUrl(String githubUrl) {
        if (!isGithubUrl(githubUrl)) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "githubUrl 必须是 GitHub 域名链接");
        }
    }

    private boolean isGithubUrl(String githubUrl) {
        if (!StringUtils.hasText(githubUrl)) {
            return false;
        }

        try {
            URI uri = new URI(githubUrl.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!StringUtils.hasText(scheme) || !StringUtils.hasText(host)) {
                return false;
            }
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            String normalizedHost = host.toLowerCase();
            return "github.com".equals(normalizedHost) || normalizedHost.endsWith(".github.com");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
