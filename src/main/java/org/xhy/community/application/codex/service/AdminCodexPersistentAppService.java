package org.xhy.community.application.codex.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.codex.assembler.CodexPersistentAssembler;
import org.xhy.community.application.codex.dto.CodexConfigSetDTO;
import org.xhy.community.application.codex.dto.CodexInstanceDTO;
import org.xhy.community.domain.codex.valueobject.CodexConfigSet;
import org.xhy.community.domain.codex.valueobject.CodexInstance;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CodexErrorCode;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminCodexPersistentAppService {
    private final SystemConfigDomainService systemConfigDomainService;

    public AdminCodexPersistentAppService(SystemConfigDomainService systemConfigDomainService) {
        this.systemConfigDomainService = systemConfigDomainService;
    }

    public CodexConfigSetDTO getAll() {
        return CodexPersistentAssembler.toDTO(loadSet());
    }

    public List<CodexInstanceDTO> listInstances() {
        return loadSet().getInstances().stream()
                .map(CodexPersistentAssembler::toDTO)
                .collect(Collectors.toList());
    }

    public CodexInstanceDTO getInstance(String id) {
        CodexInstance ins = findById(id);
        return CodexPersistentAssembler.toDTO(ins);
    }

    public CodexInstanceDTO createInstance(CodexInstanceDTO dto) {
        CodexConfigSet set = loadSet();
        CodexInstance ins = CodexPersistentAssembler.fromDTO(dto);
        if (ins.getId() == null || ins.getId().isBlank()) {
            ins.setId(UUID.randomUUID().toString());
        }
        stampUpdated(ins);
        if (set.getInstances() == null) set.setInstances(new ArrayList<>());
        set.getInstances().add(ins);
        saveSet(set);
        return CodexPersistentAssembler.toDTO(ins);
    }

    public CodexInstanceDTO updateInstance(String id, CodexInstanceDTO dto) {
        CodexConfigSet set = loadSet();
        // 找到目标实例位置
        List<CodexInstance> list = set.getInstances();
        if (list == null) list = new ArrayList<>();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) {
            throw new BusinessException(CodexErrorCode.CODEX_CONFIG_NOT_FOUND, "目标实例不存在");
        }
        // 全量覆盖式更新：Assembler fromDTO -> 强制使用路径参数 id -> 更新时间戳 -> 替换
        CodexInstance updated = CodexPersistentAssembler.fromDTO(dto);
        updated.setId(id);
        stampUpdated(updated);
        list.set(idx, updated);
        set.setInstances(list);
        saveSet(set);
        return CodexPersistentAssembler.toDTO(updated);
    }

    public void deleteInstance(String id) {
        CodexConfigSet set = loadSet();
        List<CodexInstance> list = set.getInstances();
        if (list == null || list.isEmpty()) return;
        boolean removed = list.removeIf(x -> Objects.equals(x.getId(), id));
        if (!removed) return;
        saveSet(set);
    }

    // 不再提供设置默认实例的语义

    // helpers
    private CodexConfigSet loadSet() {
        CodexConfigSet set = systemConfigDomainService.getConfigData(SystemConfigType.CODEX_CONFIGS, CodexConfigSet.class);
        if (set == null) set = new CodexConfigSet();
        if (set.getInstances() == null) set.setInstances(new ArrayList<>());
        return set;
    }
    private void saveSet(CodexConfigSet set) {
        systemConfigDomainService.updateConfigData(SystemConfigType.CODEX_CONFIGS, set);
    }
    private CodexInstance findById(String id) {
        if (id == null) return null;
        CodexConfigSet set = loadSet();
        for (CodexInstance x : set.getInstances()) {
            if (id.equals(x.getId())) return x;
        }
        return null;
    }
    private void stampUpdated(CodexInstance ins) {
        String now = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        ins.setLastUpdatedAt(now);
    }
}
