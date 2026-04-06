package org.xhy.community.domain.config.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IndependentServicesConfig {

    private List<IndependentServiceConfig> services = new ArrayList<>();

    public static IndependentServicesConfig defaultConfig() {
        IndependentServicesConfig config = new IndependentServicesConfig();
        config.setServices(new ArrayList<>(List.of(IndependentServiceConfig.defaultMockInterview())));
        return config;
    }

    public void normalize() {
        List<IndependentServiceConfig> normalized = new ArrayList<>();
        if (services != null) {
            for (IndependentServiceConfig service : services) {
                if (service != null) {
                    normalized.add(service.normalizedCopy());
                }
            }
        }

        if (normalized.isEmpty()) {
            normalized.add(IndependentServiceConfig.defaultMockInterview());
        }

        normalized.sort(Comparator
                .comparing(IndependentServiceConfig::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(IndependentServiceConfig::getServiceCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        services = normalized;
    }

    public void validate() {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("独立服务配置不能为空");
        }

        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (IndependentServiceConfig service : services) {
            if (service == null || !service.isValid()) {
                throw new IllegalArgumentException("独立服务配置数据无效");
            }
            String code = service.getServiceCode();
            if (seen.putIfAbsent(code, Boolean.TRUE) != null) {
                throw new IllegalArgumentException("独立服务编码重复: " + code);
            }
        }
    }

    public IndependentServiceConfig findByCode(String serviceCode) {
        if (services == null || serviceCode == null) {
            return null;
        }
        String normalizedCode = serviceCode.trim();
        for (IndependentServiceConfig service : services) {
            if (service != null && normalizedCode.equals(service.getServiceCode())) {
                return service;
            }
        }
        return null;
    }

    public IndependentServiceConfig findEnabledByCode(String serviceCode) {
        IndependentServiceConfig service = findByCode(serviceCode);
        if (service == null || !service.isEnabled()) {
            return null;
        }
        return service;
    }

    public List<IndependentServiceConfig> getEnabledServices() {
        if (services == null) {
            return new ArrayList<>();
        }
        List<IndependentServiceConfig> enabled = new ArrayList<>();
        for (IndependentServiceConfig service : services) {
            if (service != null && service.isEnabled()) {
                enabled.add(service);
            }
        }
        return enabled;
    }

    public List<IndependentServiceConfig> getServices() {
        return services;
    }

    public void setServices(List<IndependentServiceConfig> services) {
        this.services = services;
    }
}
