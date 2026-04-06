package org.xhy.community.application.config.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.config.assembler.IndependentServiceAssembler;
import org.xhy.community.application.config.dto.IndependentServiceDTO;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.IndependentServiceConfig;
import org.xhy.community.domain.config.valueobject.IndependentServicesConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;

import java.util.List;

@Service
public class IndependentServiceAppService {

    private final SystemConfigDomainService systemConfigDomainService;

    public IndependentServiceAppService(SystemConfigDomainService systemConfigDomainService) {
        this.systemConfigDomainService = systemConfigDomainService;
    }

    public List<IndependentServiceDTO> listPublicServices() {
        IndependentServicesConfig config = loadConfigOrDefault();
        return IndependentServiceAssembler.toDTOList(config.getEnabledServices());
    }

    public IndependentServiceDTO getPublicService(String serviceCode) {
        IndependentServicesConfig config = loadConfigOrDefault();
        IndependentServiceConfig service = config.findByCode(serviceCode);
        if (service == null) {
            IndependentServiceConfig defaultService = IndependentServicesConfig.defaultConfig().findByCode(serviceCode);
            if (defaultService != null) {
                service = defaultService;
            }
        }

        if (service == null) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_NOT_FOUND, "独立服务不存在: " + serviceCode);
        }

        return IndependentServiceAssembler.toDTO(service);
    }

    public IndependentServiceConfig getEnabledService(String serviceCode) {
        IndependentServicesConfig config = loadConfigOrDefault();
        IndependentServiceConfig service = config.findEnabledByCode(serviceCode);
        if (service == null) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_NOT_FOUND, "独立服务不存在或已停用: " + serviceCode);
        }
        return service;
    }

    private IndependentServicesConfig loadConfigOrDefault() {
        try {
            IndependentServicesConfig config = systemConfigDomainService.getConfigData(
                    SystemConfigType.INDEPENDENT_SERVICES, IndependentServicesConfig.class);
            if (config == null) {
                return IndependentServicesConfig.defaultConfig();
            }

            config.normalize();
            config.validate();
            return config;
        } catch (BusinessException e) {
            if (e.getErrorCode() == SystemConfigErrorCode.CONFIG_PARSE_ERROR) {
                return IndependentServicesConfig.defaultConfig();
            }
            throw e;
        } catch (IllegalArgumentException e) {
            return IndependentServicesConfig.defaultConfig();
        }
    }
}
