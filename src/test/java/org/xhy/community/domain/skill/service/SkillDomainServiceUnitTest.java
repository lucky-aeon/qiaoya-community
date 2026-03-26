package org.xhy.community.domain.skill.service;

import org.junit.jupiter.api.Test;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.repository.SkillRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillDomainServiceUnitTest {

    @Test
    void getSkillTitleMapByIdsShouldReturnExistingTitlesAndIgnoreMissingIds() {
        SkillEntity skill1 = new SkillEntity();
        skill1.setId("skill-1");
        skill1.setName("Workflow Skill");
        SkillEntity skill2 = new SkillEntity();
        skill2.setId("skill-2");
        skill2.setName("CLI Skill");

        RecordingSkillRepository skillRepository = new RecordingSkillRepository(List.of(skill1, skill2));
        SkillDomainService skillDomainService = new SkillDomainService(skillRepository.createProxy());

        Map<String, String> result = skillDomainService.getSkillTitleMapByIds(List.of("skill-1", "missing", "skill-2"));

        assertEquals(2, result.size());
        assertEquals("Workflow Skill", result.get("skill-1"));
        assertEquals("CLI Skill", result.get("skill-2"));
        assertTrue(result.keySet().containsAll(List.of("skill-1", "skill-2")));
    }

    @Test
    void getSkillEntityMapByIdsShouldReturnEmptyMapWhenIdsMissing() {
        RecordingSkillRepository skillRepository = new RecordingSkillRepository(List.of());
        SkillDomainService skillDomainService = new SkillDomainService(skillRepository.createProxy());

        Map<String, SkillEntity> result = skillDomainService.getSkillEntityMapByIds(List.of());

        assertTrue(result.isEmpty());
        assertFalse(skillRepository.wasInvoked());
    }

    private static final class RecordingSkillRepository implements InvocationHandler {

        private final List<SkillEntity> selectBatchIdsResult;
        private final AtomicInteger invocationCount = new AtomicInteger();

        private RecordingSkillRepository(List<SkillEntity> selectBatchIdsResult) {
            this.selectBatchIdsResult = selectBatchIdsResult;
        }

        private SkillRepository createProxy() {
            return (SkillRepository) Proxy.newProxyInstance(
                    SkillRepository.class.getClassLoader(),
                    new Class<?>[]{SkillRepository.class},
                    this
            );
        }

        private boolean wasInvoked() {
            return invocationCount.get() > 0;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("selectBatchIds".equals(method.getName())) {
                invocationCount.incrementAndGet();
                return selectBatchIdsResult;
            }
            if ("toString".equals(method.getName())) {
                return "RecordingSkillRepository";
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            throw new AssertionError("Unexpected repository method call: " + method.getName());
        }
    }
}
