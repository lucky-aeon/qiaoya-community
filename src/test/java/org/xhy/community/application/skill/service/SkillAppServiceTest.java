package org.xhy.community.application.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.skill.dto.SkillDetailDTO;
import org.xhy.community.application.skill.dto.SkillListDTO;
import org.xhy.community.application.skill.dto.SkillStatsDTO;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.repository.SkillRepository;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;
import org.xhy.community.interfaces.skill.request.UpdateSkillRequest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SkillAppServiceTest {

    @Autowired
    private SkillAppService skillAppService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserDomainService userDomainService;

    @BeforeEach
    void setUp() {
        skillRepository.selectList(null).forEach(skill -> skillRepository.deleteById(skill.getId()));
    }

    @Test
    void createAndPublicVisibility() {
        UserEntity user = createUser("creator");
        CreateSkillRequest request = buildCreateRequest("Spring AI 实战", "LangChain4j + Spring Boot", "完整技能描述");

        SkillDetailDTO created = skillAppService.createSkill(request, user.getId());
        SkillDetailDTO publicDetail = skillAppService.getPublicSkillById(created.getId());
        IPage<SkillListDTO> publicPage = skillAppService.queryPublicSkills(new SkillQueryRequest());

        assertNotNull(created.getId());
        assertEquals(user.getName(), created.getAuthorName());
        assertEquals(created.getId(), publicDetail.getId());
        assertEquals("完整技能描述", publicDetail.getDescription());
        assertEquals(1L, publicPage.getTotal());
        assertEquals(created.getId(), publicPage.getRecords().get(0).getId());
    }

    @Test
    void statsTotalCount() {
        UserEntity user = createUser("stats");
        skillAppService.createSkill(buildCreateRequest("技能A", "摘要A", "描述A"), user.getId());
        skillAppService.createSkill(buildCreateRequest("技能B", "摘要B", "描述B"), user.getId());

        SkillStatsDTO stats = skillAppService.getSkillStats();

        assertEquals(2L, stats.getTotalCount());
    }

    @Test
    void keywordQueryAndCreateTimeDescOrdering() {
        UserEntity user = createUser("query");
        SkillDetailDTO older = skillAppService.createSkill(
                buildCreateRequest("Java 面试官", "擅长并发与 JVM", "旧技能"),
                user.getId()
        );
        SkillDetailDTO newer = skillAppService.createSkill(
                buildCreateRequest("Java 架构师", "微服务与并发编程", "新技能"),
                user.getId()
        );

        SkillEntity olderEntity = skillRepository.selectById(older.getId());
        olderEntity.setCreateTime(LocalDateTime.now().minusDays(1));
        skillRepository.updateById(olderEntity);

        SkillQueryRequest request = new SkillQueryRequest();
        request.setKeyword("Java");
        IPage<SkillListDTO> result = skillAppService.queryPublicSkills(request);

        assertEquals(2L, result.getTotal());
        assertEquals(newer.getId(), result.getRecords().get(0).getId());
        assertEquals(older.getId(), result.getRecords().get(1).getId());
        assertTrue(result.getRecords().stream().allMatch(item -> item.getName().contains("Java")));
    }

    @Test
    void nonAuthorUpdateRejected() {
        UserEntity author = createUser("author");
        UserEntity otherUser = createUser("other");
        SkillDetailDTO created = skillAppService.createSkill(
                buildCreateRequest("技能所有权", "作者专属", "描述"),
                author.getId()
        );

        BusinessException exception = assertThrows(BusinessException.class, () ->
                skillAppService.updateSkill(created.getId(), buildUpdateRequest("被篡改", "摘要", "描述"), otherUser.getId())
        );

        assertEquals("只能操作自己的技能", exception.getMessage());
    }

    @Test
    void nonGithubUrlRejected() {
        UserEntity user = createUser("github");
        CreateSkillRequest request = buildCreateRequest("非法链接技能", "摘要", "描述");
        request.setGithubUrl("https://gitlab.com/org/repo");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                skillAppService.createSkill(request, user.getId())
        );

        assertEquals("githubUrl 必须是 GitHub 域名链接", exception.getMessage());
    }

    private UserEntity createUser(String prefix) {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        return userDomainService.registerUser(email, "123456");
    }

    private CreateSkillRequest buildCreateRequest(String name, String summary, String description) {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName(name);
        request.setSummary(summary);
        request.setDescription(description);
        request.setGithubUrl("https://github.com/openai/codex");
        return request;
    }

    private UpdateSkillRequest buildUpdateRequest(String name, String summary, String description) {
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setName(name);
        request.setSummary(summary);
        request.setDescription(description);
        request.setGithubUrl("https://github.com/openai/codex-java");
        return request;
    }
}
