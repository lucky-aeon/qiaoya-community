package org.xhy.community.infrastructure.permission;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.application.subscription.dto.PermissionOptionDTO;

import java.util.*;

/**
 * 扫描项目中 @RequiresPlanPermissions 注解，收集全部权限码
 */
@Component
public class PermissionCodeScanner {
    private static final Logger log = LoggerFactory.getLogger(PermissionCodeScanner.class);

    private final RequestMappingHandlerMapping handlerMapping;
    private final Map<String, String> codeNameMap = new TreeMap<>(); // code -> name

    public PermissionCodeScanner(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @PostConstruct
    public void scan() {
        try {
            handlerMapping.getHandlerMethods().forEach((info, handler) -> collect(handler));
            log.info("[PermissionCodeScanner] 已发现权限码 {} 个：{}", codeNameMap.size(), codeNameMap.keySet());
        } catch (Exception e) {
            log.warn("[PermissionCodeScanner] 扫描失败：{}", e.getMessage());
        }
    }

    private void collect(HandlerMethod handler) {
        RequiresPlanPermissions onMethod = handler.getMethodAnnotation(RequiresPlanPermissions.class);
        putFromAnnotation(onMethod);
        RequiresPlanPermissions onType = handler.getBeanType().getAnnotation(RequiresPlanPermissions.class);
        putFromAnnotation(onType);
    }

    private void putFromAnnotation(RequiresPlanPermissions ann) {
        if (ann == null) return;
        // items 优先，带中文名
        if (ann.items() != null) {
            for (RequiresPlanPermissions.Item it : ann.items()) {
                if (it == null) continue;
                String code = nv(it.code());
                String name = nv(it.name());
                if (!code.isBlank()) {
                    codeNameMap.putIfAbsent(code, name.isBlank() ? code : name);
                }
            }
        }
        if (ann.value() != null) {
            for (String code : ann.value()) {
                if (code == null) continue;
                String c = code.trim();
                if (!c.isBlank()) {
                    codeNameMap.putIfAbsent(c, c);
                }
            }
        }
    }

    private String nv(String s) { return s == null ? "" : s.trim(); }

    /** 返回扫描得到的全部权限码（去重、按字典序） */
    public List<String> getDiscoveredCodes() {
        return new ArrayList<>(codeNameMap.keySet());
    }

    /** 返回扫描得到的权限选项（code + label） */
    public List<PermissionOptionDTO> getDiscoveredOptions() {
        List<PermissionOptionDTO> list = new ArrayList<>();
        for (Map.Entry<String, String> e : codeNameMap.entrySet()) {
            list.add(new PermissionOptionDTO(e.getKey(), e.getValue(), ""));
        }
        return list;
    }
}
