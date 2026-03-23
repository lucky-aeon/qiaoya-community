package org.xhy.community.domain.skill.service;

import org.junit.jupiter.api.Test;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.repository.SkillRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SkillErrorCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkillDomainServiceTest {

    @Test
    void updateSkill_shouldUpdateContentWhenCurrentUserIsOwner() {
        SkillRepositoryStub repositoryStub = new SkillRepositoryStub();
        SkillDomainService skillDomainService = new SkillDomainService(repositoryStub.createRepository());

        SkillEntity existing = new SkillEntity("user-1", "旧名称", "旧简介", "旧描述", "https://github.com/demo/old");
        existing.setId("skill-1");
        repositoryStub.selectedSkill = existing;

        SkillEntity updated = new SkillEntity("ignored", "新名称", "新简介", "新描述", "https://github.com/demo/new");
        updated.setId("skill-1");

        SkillEntity result = skillDomainService.updateSkill(updated, "user-1");

        assertSame(existing, result);
        assertSame(existing, repositoryStub.updatedSkill);
        assertEquals("新名称", repositoryStub.updatedSkill.getName());
        assertEquals("新简介", repositoryStub.updatedSkill.getSummary());
        assertEquals("新描述", repositoryStub.updatedSkill.getDescription());
        assertEquals("https://github.com/demo/new", repositoryStub.updatedSkill.getGithubUrl());
    }

    @Test
    void deleteSkill_shouldRejectWhenCurrentUserIsNotOwner() {
        SkillRepositoryStub repositoryStub = new SkillRepositoryStub();
        SkillDomainService skillDomainService = new SkillDomainService(repositoryStub.createRepository());

        SkillEntity existing = new SkillEntity("user-1", "名称", "简介", "描述", "https://github.com/demo/repo");
        existing.setId("skill-1");
        repositoryStub.selectedSkill = existing;

        BusinessException exception = assertThrows(BusinessException.class,
                () -> skillDomainService.deleteSkill("skill-1", "user-2"));

        assertEquals(SkillErrorCode.UNAUTHORIZED_MODIFY.getCode(), exception.getCode());
        assertNull(repositoryStub.deletedId);
    }

    @Test
    void updateSkill_shouldThrowWhenSkillDoesNotExist() {
        SkillRepositoryStub repositoryStub = new SkillRepositoryStub();
        SkillDomainService skillDomainService = new SkillDomainService(repositoryStub.createRepository());

        SkillEntity updated = new SkillEntity("user-1", "名称", "简介", "描述", "https://github.com/demo/repo");
        updated.setId("missing-skill");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> skillDomainService.updateSkill(updated, "user-1"));

        assertEquals(SkillErrorCode.SKILL_NOT_FOUND.getCode(), exception.getCode());
    }

    private static final class SkillRepositoryStub implements InvocationHandler {
        private SkillEntity selectedSkill;
        private SkillEntity updatedSkill;
        private String deletedId;

        private SkillRepository createRepository() {
            return (SkillRepository) Proxy.newProxyInstance(
                    SkillRepository.class.getClassLoader(),
                    new Class[]{SkillRepository.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "selectById" -> selectedSkill;
                case "updateById" -> {
                    updatedSkill = (SkillEntity) args[0];
                    yield 1;
                }
                case "deleteById" -> {
                    deletedId = String.valueOf(args[0]);
                    yield 1;
                }
                case "toString" -> "SkillRepositoryStub";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unexpected method: " + method.getName());
            };
        }
    }
}
