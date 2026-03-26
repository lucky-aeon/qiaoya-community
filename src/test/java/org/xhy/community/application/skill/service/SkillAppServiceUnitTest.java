package org.xhy.community.application.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xhy.community.application.skill.dto.SkillDetailDTO;
import org.xhy.community.application.skill.dto.SkillListDTO;
import org.xhy.community.application.skill.dto.SkillStatsDTO;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.query.SkillQuery;
import org.xhy.community.domain.skill.service.SkillDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillAppServiceUnitTest {

    private FakeSkillDomainService skillDomainService;
    private FakeUserDomainService userDomainService;
    private SkillAppService skillAppService;

    @BeforeEach
    void setUp() {
        skillDomainService = new FakeSkillDomainService();
        userDomainService = new FakeUserDomainService();
        skillAppService = new SkillAppService(skillDomainService, userDomainService);
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
        userDomainService.userMap = Map.of("user-1", author);

        IPage<SkillListDTO> result = skillAppService.queryMySkills("user-1", request);

        assertEquals("user-1", skillDomainService.lastQuery.getUserId());
        assertEquals("workflow", skillDomainService.lastQuery.getKeyword());
        assertEquals(2, skillDomainService.lastQuery.getPageNum());
        assertEquals(5, skillDomainService.lastQuery.getPageSize());
        assertEquals(1, result.getRecords().size());
        assertEquals("Alice", result.getRecords().get(0).getAuthorName());
        assertEquals("Workflow Skill", result.getRecords().get(0).getName());
    }

    @Test
    void getPublicSkillByIdShouldFillAuthorName() {
        SkillEntity skill = buildSkill("skill-2", "user-2", "CLI Skill", "命令行交付", "https://github.com/acme/cli-skill");
        skillDomainService.skillById = skill;

        UserEntity author = new UserEntity();
        author.setId("user-2");
        author.setName("Bob");
        userDomainService.userById = author;

        SkillDetailDTO detail = skillAppService.getPublicSkillById("skill-2");

        assertEquals("skill-2", detail.getId());
        assertEquals("CLI Skill", detail.getName());
        assertEquals("Bob", detail.getAuthorName());
        assertEquals("https://github.com/acme/cli-skill", detail.getGithubUrl());
        assertEquals("命令行交付", detail.getDescription());
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
        private Long countValue = 0L;

        FakeSkillDomainService() {
            super(null);
        }

        @Override
        public IPage<SkillEntity> querySkills(SkillQuery query) {
            this.lastQuery = query;
            return queryResult;
        }

        @Override
        public SkillEntity getSkillById(String skillId) {
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
}
