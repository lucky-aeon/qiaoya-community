package org.xhy.community.domain.skill.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.repository.SkillRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SkillDomainServiceUnitTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillDomainService skillDomainService;

    @Test
    void getSkillTitleMapByIdsShouldReturnExistingTitlesAndIgnoreMissingIds() {
        SkillEntity skill1 = new SkillEntity();
        skill1.setId("skill-1");
        skill1.setName("Workflow Skill");
        SkillEntity skill2 = new SkillEntity();
        skill2.setId("skill-2");
        skill2.setName("CLI Skill");

        doReturn(List.of(skill1, skill2)).when(skillRepository).selectBatchIds(anyCollection());

        Map<String, String> result = skillDomainService.getSkillTitleMapByIds(List.of("skill-1", "missing", "skill-2"));

        assertEquals(2, result.size());
        assertEquals("Workflow Skill", result.get("skill-1"));
        assertEquals("CLI Skill", result.get("skill-2"));
        assertTrue(result.keySet().containsAll(List.of("skill-1", "skill-2")));
    }

    @Test
    void getSkillEntityMapByIdsShouldReturnEmptyMapWhenIdsMissing() {
        Map<String, SkillEntity> result = skillDomainService.getSkillEntityMapByIds(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(skillRepository);
    }
}
