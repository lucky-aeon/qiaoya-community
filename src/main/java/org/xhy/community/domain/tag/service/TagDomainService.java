package org.xhy.community.domain.tag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;
import org.xhy.community.domain.tag.entity.TagScopeEntity;
import org.xhy.community.domain.tag.entity.UserTagAssignmentEntity;
import org.xhy.community.domain.tag.repository.TagDefinitionRepository;
import org.xhy.community.domain.tag.repository.TagScopeRepository;
import org.xhy.community.domain.tag.repository.UserTagAssignmentRepository;
import org.xhy.community.domain.tag.valueobject.TagAssignmentStatus;
import org.xhy.community.domain.tag.valueobject.TagSourceType;
import org.xhy.community.domain.tag.valueobject.TagTargetType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.TagErrorCode;
import org.xhy.community.domain.tag.query.TagQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagDomainService {

    private final TagDefinitionRepository tagDefinitionRepository;
    private final TagScopeRepository tagScopeRepository;
    private final UserTagAssignmentRepository userTagAssignmentRepository;
    private static final Logger log = LoggerFactory.getLogger(TagDomainService.class);

    public TagDomainService(TagDefinitionRepository tagDefinitionRepository,
                            TagScopeRepository tagScopeRepository,
                            UserTagAssignmentRepository userTagAssignmentRepository) {
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.tagScopeRepository = tagScopeRepository;
        this.userTagAssignmentRepository = userTagAssignmentRepository;
    }

    // ========== 标签定义（管理员） ==========

    /** 创建标签（包含唯一 code 校验） */
    public TagDefinitionEntity createTag(TagDefinitionEntity entity) {
        if (entity == null) return null;
        boolean exists = tagDefinitionRepository.exists(new LambdaQueryWrapper<TagDefinitionEntity>()
                .eq(TagDefinitionEntity::getCode, entity.getCode())
        );
        if (exists) {
            throw new BusinessException(TagErrorCode.TAG_CODE_EXISTS);
        }
        tagDefinitionRepository.insert(entity);
        log.info("【标签】已创建：tagId={}, code={}, name={}", entity.getId(), entity.getCode(), entity.getName());
        return entity;
    }

    /** 更新标签定义（按ID），不存在则抛错 */
    public TagDefinitionEntity updateTag(TagDefinitionEntity entity) {
        if (entity == null || entity.getId() == null) return entity;
        TagDefinitionEntity exist = tagDefinitionRepository.selectById(entity.getId());
        if (exist == null) {
            log.warn("【标签】更新失败：标签不存在，tagId={}", entity.getId());
            throw new BusinessException(TagErrorCode.TAG_NOT_FOUND);
        }
        exist.setName(entity.getName());
        exist.setCategory(entity.getCategory());
        exist.setIconUrl(entity.getIconUrl());
        exist.setDescription(entity.getDescription());
        if (entity.getPublicVisible() != null) exist.setPublicVisible(entity.getPublicVisible());
        if (entity.getUniquePerUser() != null) exist.setUniquePerUser(entity.getUniquePerUser());
        if (entity.getEnabled() != null) exist.setEnabled(entity.getEnabled());
        tagDefinitionRepository.updateById(exist);
        log.info("【标签】已更新：tagId={}, name={}", exist.getId(), exist.getName());
        return exist;
    }

    /** 分页查询标签定义（TagQuery 保证有默认分页参数） */
    public IPage<TagDefinitionEntity> listTags(TagQuery query) {
        Page<TagDefinitionEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TagDefinitionEntity> wrapper = new LambdaQueryWrapper<TagDefinitionEntity>()
                .like(StringUtils.hasText(query.getName()), TagDefinitionEntity::getName, query.getName())
                .eq(StringUtils.hasText(query.getCategory()), TagDefinitionEntity::getCategory, query.getCategory())
                .eq(query.getEnabled() != null, TagDefinitionEntity::getEnabled, query.getEnabled())
                .orderByDesc(TagDefinitionEntity::getCreateTime);
        return tagDefinitionRepository.selectPage(page, wrapper);
    }

    /** 查询某标签的绑定范围列表 */
    public java.util.List<TagScopeEntity> listScopesByTagId(String tagId) {
        if (tagId == null || tagId.isBlank()) return java.util.List.of();
        return tagScopeRepository.selectList(new LambdaQueryWrapper<TagScopeEntity>()
                .eq(TagScopeEntity::getTagId, tagId)
                .orderByDesc(TagScopeEntity::getCreateTime)
        );
    }

    /** 添加可见范围（幂等）：若存在相同 scope 则忽略 */
    public void addScope(TagScopeEntity scope) {
        if (scope == null || scope.getTagId() == null) return;
        TagDefinitionEntity tag = tagDefinitionRepository.selectById(scope.getTagId());
        if (tag == null) {
            log.warn("【标签】添加范围失败：标签不存在，tagId={}", scope.getTagId());
            throw new BusinessException(TagErrorCode.TAG_NOT_FOUND);
        }
        Long cnt = tagScopeRepository.selectCount(new LambdaQueryWrapper<TagScopeEntity>()
                .eq(TagScopeEntity::getTagId, scope.getTagId())
                .eq(TagScopeEntity::getTargetType, scope.getTargetType())
                .eq(TagScopeEntity::getTargetId, scope.getTargetId())
        );
        if (cnt != null && cnt > 0) return;
        tagScopeRepository.insert(scope);
        log.info("【标签】已添加范围：tagId={}, targetType={}, targetId={}",
                scope.getTagId(), scope.getTargetType(), scope.getTargetId());
    }

    /** 根据ID删除可见范围 */
    public void removeScope(String scopeId) {
        tagScopeRepository.deleteById(scopeId);
    }

    /**
     * 课程完成发放标签（幂等）
     */
    public void issueCourseCompletionTag(String userId, String courseId) {
        // 找到与课程绑定的标签
        List<TagScopeEntity> scopes = tagScopeRepository.selectList(new LambdaQueryWrapper<TagScopeEntity>()
                .eq(TagScopeEntity::getTargetType, TagTargetType.COURSE)
                .eq(TagScopeEntity::getTargetId, courseId)
        );
        if (scopes == null || scopes.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        ArrayList<UserTagAssignmentEntity> userTagAssignmentEntities = new ArrayList<>();
        for (TagScopeEntity scope : scopes) {
            String tagId = scope.getTagId();
            if (tagId == null) continue;

            UserTagAssignmentEntity userTagAssignmentEntity = new UserTagAssignmentEntity();
            userTagAssignmentEntity.setUserId(userId);
            userTagAssignmentEntity.setTagId(tagId);
            userTagAssignmentEntity.setStatus(TagAssignmentStatus.ISSUED);
            userTagAssignmentEntity.setIssuedAt(now);
            userTagAssignmentEntity.setSourceType(TagSourceType.COURSE_COMPLETION);
            userTagAssignmentEntity.setSourceId(courseId);
            userTagAssignmentEntities.add(userTagAssignmentEntity);
        }
        userTagAssignmentRepository.insert(userTagAssignmentEntities);
        log.info("【标签】课程完成发放：userId={}, courseId={}, tags={}个", userId, courseId, userTagAssignmentEntities.size());
    }

    /**
     * 是否已拥有“完成课程{courseId}”对应的标签
     */
    public boolean hasCourseCompletionTag(String userId, String courseId) {
        List<TagScopeEntity> scopes = tagScopeRepository.selectList(new LambdaQueryWrapper<TagScopeEntity>()
                .eq(TagScopeEntity::getTargetType, TagTargetType.COURSE)
                .eq(TagScopeEntity::getTargetId, courseId)
        );
        if (scopes == null || scopes.isEmpty()) return false;
        List<String> tagIds = scopes.stream().map(TagScopeEntity::getTagId).toList();
        Long cnt = userTagAssignmentRepository.selectCount(new LambdaQueryWrapper<UserTagAssignmentEntity>()
                .eq(UserTagAssignmentEntity::getUserId, userId)
                .in(UserTagAssignmentEntity::getTagId, tagIds)
                .eq(UserTagAssignmentEntity::getStatus, TagAssignmentStatus.ISSUED)
        );
        return cnt != null && cnt > 0;
    }

    /** 列出用户已发放的标签授予记录（按 issuedAt 降序） */
    public List<UserTagAssignmentEntity> listIssuedAssignmentsByUser(String userId) {
        return userTagAssignmentRepository.selectList(new LambdaQueryWrapper<UserTagAssignmentEntity>()
                .eq(UserTagAssignmentEntity::getUserId, userId)
                .eq(UserTagAssignmentEntity::getStatus, TagAssignmentStatus.ISSUED)
                .orderByDesc(UserTagAssignmentEntity::getIssuedAt)
        );
    }

    /** 批量加载标签定义并以ID为键返回映射 */
    public Map<String, TagDefinitionEntity> getTagDefinitionMapByIds(Collection<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return java.util.Collections.emptyMap();
        List<TagDefinitionEntity> defs = tagDefinitionRepository.selectBatchIds(tagIds);
        return defs.stream().collect(Collectors.toMap(TagDefinitionEntity::getId, d -> d));
    }

    /**
     * 手动为用户发放标签（幂等）。
     * - 验证标签存在且启用
     * - 若已存在记录则更新为 ISSUED 并刷新发放时间与来源
     */
    public void assignTagToUser(String userId, String tagId, TagSourceType sourceType, String sourceId) {
        TagDefinitionEntity def = tagDefinitionRepository.selectById(tagId);
        if (def == null || Boolean.FALSE.equals(def.getEnabled())) {
            log.warn("【标签】发放失败：标签不存在或未启用，userId={}, tagId={}", userId, tagId);
            return; // 标签不存在或未启用，安全返回
        }
        LocalDateTime now = LocalDateTime.now();
        UserTagAssignmentEntity exist = userTagAssignmentRepository.selectOne(
                new LambdaQueryWrapper<UserTagAssignmentEntity>()
                        .eq(UserTagAssignmentEntity::getUserId, userId)
                        .eq(UserTagAssignmentEntity::getTagId, tagId)
        );
        UserTagAssignmentEntity e = new UserTagAssignmentEntity();
        if (exist != null) {
            e.setId(exist.getId());
        }
        e.setUserId(userId);
        e.setTagId(tagId);
        e.setStatus(TagAssignmentStatus.ISSUED);
        e.setIssuedAt(now);
        e.setSourceType(sourceType != null ? sourceType : TagSourceType.MANUAL);
        e.setSourceId(sourceId != null ? sourceId : (exist != null ? exist.getSourceId() : null));
        userTagAssignmentRepository.insertOrUpdate(e);
        log.info("【标签】已发放：userId={}, tagId={}, sourceType={}, sourceId={}",
                userId, tagId, e.getSourceType(), e.getSourceId());
    }

    /** 撤销用户标签（若存在）。 */
    public void revokeTagFromUser(String userId, String tagId) {
        UserTagAssignmentEntity exist = userTagAssignmentRepository.selectOne(
                new LambdaQueryWrapper<UserTagAssignmentEntity>()
                        .eq(UserTagAssignmentEntity::getUserId, userId)
                        .eq(UserTagAssignmentEntity::getTagId, tagId)
        );
        if (exist != null && exist.getStatus() != TagAssignmentStatus.REVOKED) {
            exist.setStatus(TagAssignmentStatus.REVOKED);
            exist.setRevokedAt(LocalDateTime.now());
            userTagAssignmentRepository.updateById(exist);
            log.info("【标签】已撤销：userId={}, tagId={}", userId, tagId);
        }
    }
}
