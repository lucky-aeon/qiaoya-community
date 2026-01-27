package org.xhy.community.application.codex.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.codex.dto.CodexConfigSetDTO;
import org.xhy.community.application.codex.dto.CodexInstanceDTO;
import org.xhy.community.domain.codex.valueobject.CodexConfigSet;
import org.xhy.community.domain.codex.valueobject.CodexInstance;

import java.util.ArrayList;
import java.util.List;

public class CodexPersistentAssembler {
    private CodexPersistentAssembler() {}

    public static CodexInstanceDTO toDTO(CodexInstance entity) {
        if (entity == null) return null;
        CodexInstanceDTO dto = new CodexInstanceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static CodexInstance fromDTO(CodexInstanceDTO dto) {
        if (dto == null) return null;
        CodexInstance e = new CodexInstance();
        BeanUtils.copyProperties(dto, e);
        return e;
    }

    public static CodexConfigSetDTO toDTO(CodexConfigSet set) {
        if (set == null) return null;
        CodexConfigSetDTO dto = new CodexConfigSetDTO();
        List<CodexInstanceDTO> list = new ArrayList<>();
        if (set.getInstances() != null) {
            for (CodexInstance ins: set.getInstances()) {
                list.add(toDTO(ins));
            }
        }
        dto.setInstances(list);
        return dto;
    }

    public static CodexConfigSet fromDTO(CodexConfigSetDTO dto) {
        if (dto == null) return null;
        CodexConfigSet set = new CodexConfigSet();
        List<CodexInstance> list = new ArrayList<>();
        if (dto.getInstances() != null) {
            for (CodexInstanceDTO d : dto.getInstances()) {
                list.add(fromDTO(d));
            }
        }
        set.setInstances(list);
        return set;
    }
}
