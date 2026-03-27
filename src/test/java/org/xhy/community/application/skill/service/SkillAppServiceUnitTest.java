package org.xhy.community.application.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillAppServiceUnitTest {

    private FakeSkillDomainService skillDomainService;
    private FakeUserDomainService userDomainService;
    private FakeLikeDomainService likeDomainService;
    private FakeFavoriteDomainService favoriteDomainService;
    private FakeCommentDomainService commentDomainService;
    private SkillAppService skillAppService;

    @BeforeEach
    void setUp() {
        skillDomainService = new FakeSkillDomainService();
        userDomainService = new FakeUserDomainService();
        likeDomainService = new FakeLikeDomainService();
        favoriteDomainService = new FakeFavoriteDomainService();
        commentDomainService = new FakeCommentDomainService();
        skillAppService = new SkillAppService(
                skillDomainService,
                userDomainService,
                likeDomainService,
                favoriteDomainService,
                commentDomainService
        );
    }

    @Test
    void createSkillShouldRejectNonGithubUrl() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Prompt Pack");
        request.setSummary("一组工作流");
        request.setDescription("详情");
        request.setGithubUrl("https://gitlab.com/example/repo");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skillAppService.createSkill(request, "user-1")
        );

        assertTrue(exception.getMessage().contains("GitHub"));
    }

    @Test
    void queryMySkillsShouldCarryCurrentUserAndMapAuthorName() {
        SkillQueryRequest request = new SkillQueryRequest();
        request.setPageNum(2);
        request.setPageSize(5);
        request.setKeyword("workflow");

        SkillEntity skill = buildSkill("skill-1", "user-1", "Workflow Skill", "自动化工作流", "https://github.com/acme/workflow-skill");
        skillDomainService.queryResult = pageOf(2, 5, List.of(skill));

        UserEntity author = new UserEntity();
        author.setId("user-1");
        author.setName("Alice");
        author.setAvatar("https://cdn.example.com/alice.png");
        author.setDescription("专注自动化工作流");
        userDomainService.userMap = Map.of("user-1", author);

        IPage<SkillListDTO> result = skillAppService.queryMySkills("user-1", request);

        assertEquals("user-1", skillDomainService.lastQuery.getUserId());
        assertEquals("workflow", skillDomainService.lastQuery.getKeyword());
        assertEquals(2, skillDomainService.lastQuery.getPageNum());
        assertEquals(5, skillDomainService.lastQuery.getPageSize());
        assertEquals(1, result.getRecords().size());
        assertEquals("Alice", result.getRecords().get(0).getAuthorName());
        assertEquals("https://cdn.example.com/alice.png", result.getRecords().get(0).getAuthorAvatar());
        assertEquals("专注自动化工作流", result.getRecords().get(0).getAuthorDescription());
        assertEquals("Workflow Skill", result.getRecords().get(0).getName());
    }

    @Test
    void queryPublicSkillsShouldFillInteractionCountsAndAuthorProfile() {
        SkillQueryRequest request = new SkillQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        SkillEntity skill = buildSkill("skill-1", "user-1", "Workflow Skill", "自动化工作流", "https://github.com/acme/workflow-skill");
        skillDomainService.queryResult = pageOf(1, 10, List.of(skill));

        UserEntity author = new UserEntity();
        author.setId("user-1");
        author.setName("Alice");
        author.setAvatar("https://cdn.example.com/alice.png");
        author.setDescription("专注自动化工作流");
        userDomainService.userMap = Map.of("user-1", author);
        likeDomainService.batchCountMap = Map.of("SKILL:skill-1", 3L);
        favoriteDomainService.batchCountMap = Map.of("SKILL:skill-1", 5L);
        commentDomainService.batchCountMap = Map.of("skill-1", 7L);

        IPage<SkillListDTO> result = skillAppService.queryPublicSkills(request);

        assertEquals("Alice", result.getRecords().get(0).getAuthorName());
        assertEquals("https://cdn.example.com/alice.png", result.getRecords().get(0).getAuthorAvatar());
        assertEquals("专注自动化工作流", result.getRecords().get(0).getAuthorDescription());
        assertEquals(3L, result.getRecords().get(0).getLikeCount());
        assertEquals(5L, result.getRecords().get(0).getFavoriteCount());
        assertEquals(7L, result.getRecords().get(0).getCommentCount());
        assertEquals(Map.of("skill-1", LikeTargetType.SKILL), likeDomainService.lastBatchTargets);
        assertEquals(Map.of("skill-1", FavoriteTargetType.SKILL), favoriteDomainService.lastBatchTargets);
        assertEquals(Set.of("skill-1"), commentDomainService.lastBatchBusinessIds);
        assertEquals(BusinessType.SKILL, commentDomainService.lastBusinessType);
    }

    @Test
    void queryPublicSkillsShouldReturnEmptyRecordsWhenNoSkillExists() {
        SkillQueryRequest request = new SkillQueryRequest();
        skillDomainService.queryResult = pageOf(1, 10, List.of());

        IPage<SkillListDTO> result = skillAppService.queryPublicSkills(request);

        assertTrue(result.getRecords().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    void getPublicSkillByIdShouldFillAuthorProfile() {
        SkillEntity skill = buildSkill("skill-2", "user-2", "CLI Skill", "命令行交付", "https://github.com/acme/cli-skill");
        skillDomainService.skillById = skill;

        UserEntity author = new UserEntity();
        author.setId("user-2");
        author.setName("Bob");
        author.setAvatar("https://cdn.example.com/bob.png");
        author.setDescription("命令行工具作者");
        userDomainService.userById = author;

        SkillDetailDTO detail = skillAppService.getPublicSkillById("skill-2");

        assertEquals("skill-2", detail.getId());
        assertEquals("CLI Skill", detail.getName());
        assertEquals("Bob", detail.getAuthorName());
        assertEquals("https://cdn.example.com/bob.png", detail.getAuthorAvatar());
        assertEquals("命令行工具作者", detail.getAuthorDescription());
        assertEquals("https://github.com/acme/cli-skill", detail.getGithubUrl());
        assertEquals("命令行交付", detail.getDescription());
    }

    @Test
    void getPublicSkillByIdShouldFillLikeFavoriteAndCommentCounts() {
        SkillEntity skill = buildSkill("skill-3", "user-3", "CLI Skill", "命令行交付", "https://github.com/acme/cli-skill");
        skillDomainService.skillById = skill;

        UserEntity author = new UserEntity();
        author.setId("user-3");
        author.setName("Carol");
        author.setAvatar("https://cdn.example.com/carol.png");
        author.setDescription("CLI 与平台集成");
        userDomainService.userById = author;
        likeDomainService.singleLikeCount = 11L;
        favoriteDomainService.singleFavoriteCount = 13L;
        commentDomainService.singleCommentCount = 17L;

        SkillDetailDTO detail = skillAppService.getPublicSkillById("skill-3");

        assertEquals(11L, detail.getLikeCount());
        assertEquals(13L, detail.getFavoriteCount());
        assertEquals(17L, detail.getCommentCount());
        assertEquals("Carol", detail.getAuthorName());
        assertEquals("https://cdn.example.com/carol.png", detail.getAuthorAvatar());
        assertEquals("CLI 与平台集成", detail.getAuthorDescription());
    }

    @Test
    void getPublicSkillByIdShouldPropagateSkillNotFound() {
        skillDomainService.skillByIdException = new BusinessException(ValidationErrorCode.PARAM_INVALID, "技能不存在");

        assertThrows(BusinessException.class, () -> skillAppService.getPublicSkillById("missing"));
    }

    @Test
    void getSkillStatsShouldExposeDomainCount() {
        skillDomainService.countValue = 7L;

        SkillStatsDTO stats = skillAppService.getSkillStats();

        assertEquals(7L, stats.getTotalCount());
    }

    private Page<SkillEntity> pageOf(long current, long size, List<SkillEntity> records) {
        Page<SkillEntity> page = new Page<>(current, size, records.size());
        page.setRecords(records);
        return page;
    }

    private SkillEntity buildSkill(String id, String userId, String name, String description, String githubUrl) {
        SkillEntity skill = new SkillEntity(userId, name, "摘要-" + name, description, githubUrl);
        skill.setId(id);
        skill.setCreateTime(LocalDateTime.of(2026, 3, 26, 10, 0));
        skill.setUpdateTime(LocalDateTime.of(2026, 3, 26, 10, 5));
        return skill;
    }

    private static class FakeSkillDomainService extends SkillDomainService {
        private SkillQuery lastQuery;
        private IPage<SkillEntity> queryResult = new Page<>();
        private SkillEntity skillById;
        private BusinessException skillByIdException;
        private Long countValue = 0L;
        private Set<String> lastQueryIds = Set.of();

        FakeSkillDomainService() {
            super(null);
        }

        @Override
        public IPage<SkillEntity> querySkills(SkillQuery query) {
            this.lastQuery = query;
            this.lastQueryIds = queryResult.getRecords().stream()
                    .map(SkillEntity::getId)
                    .collect(java.util.stream.Collectors.toSet());
            return queryResult;
        }

        @Override
        public SkillEntity getSkillById(String skillId) {
            if (skillByIdException != null) {
                throw skillByIdException;
            }
            return skillById;
        }

        @Override
        public Long countSkills() {
            return countValue;
        }
    }

    private static class FakeUserDomainService extends UserDomainService {
        private UserEntity userById;
        private Map<String, UserEntity> userMap = Map.of();

        FakeUserDomainService() {
            super(null, null, null, null);
        }

        @Override
        public UserEntity getUserById(String userId) {
            return userById;
        }

        @Override
        public Map<String, UserEntity> getUserEntityMapByIds(Collection<String> userIds) {
            return userMap;
        }
    }

    private static class FakeLikeDomainService extends LikeDomainService {
        private Map<String, Long> batchCountMap = Map.of();
        private Map<String, LikeTargetType> lastBatchTargets = Map.of();
        private Long singleLikeCount = 0L;

        FakeLikeDomainService() {
            super(null);
        }

        @Override
        public Map<String, Long> batchCountLikes(Map<String, LikeTargetType> targets) {
            lastBatchTargets = targets;
            return batchCountMap;
        }

        @Override
        public long countLikes(String targetId, LikeTargetType targetType) {
            return singleLikeCount;
        }
    }

    private static class FakeFavoriteDomainService extends FavoriteDomainService {
        private Map<String, Long> batchCountMap = Map.of();
        private Map<String, FavoriteTargetType> lastBatchTargets = Map.of();
        private Long singleFavoriteCount = 0L;

        FakeFavoriteDomainService() {
            super(null);
        }

        @Override
        public Map<String, Long> batchCountFavorites(Map<String, FavoriteTargetType> targets) {
            lastBatchTargets = targets;
            return batchCountMap;
        }

        @Override
        public long countFavorites(String targetId, FavoriteTargetType targetType) {
            return singleFavoriteCount;
        }
    }

    private static class FakeCommentDomainService extends CommentDomainService {
        private Map<String, Long> batchCountMap = Map.of();
        private Set<String> lastBatchBusinessIds = Set.of();
        private BusinessType lastBusinessType;
        private Long singleCommentCount = 0L;

        FakeCommentDomainService() {
            super(null, null);
        }

        @Override
        public Map<String, Long> getCommentCountMapByBusinessIds(Collection<String> businessIds, BusinessType businessType) {
            lastBatchBusinessIds = businessIds == null ? Set.of() : Set.copyOf(businessIds);
            lastBusinessType = businessType;
            return batchCountMap;
        }

        @Override
        public Long getCommentCountByBusiness(String businessId, BusinessType businessType) {
            return singleCommentCount;
        }
    }
}
