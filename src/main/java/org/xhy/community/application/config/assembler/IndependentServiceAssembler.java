package org.xhy.community.application.config.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.config.dto.IndependentServiceDTO;
import org.xhy.community.domain.config.valueobject.IndependentServiceConfig;

import java.util.List;
import java.util.stream.Collectors;

public class IndependentServiceAssembler {

    public static IndependentServiceDTO toDTO(IndependentServiceConfig config) {
        if (config == null) {
            return null;
        }

        IndependentServiceDTO dto = new IndependentServiceDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }

    public static List<IndependentServiceDTO> toDTOList(List<IndependentServiceConfig> configs) {
        if (configs == null) {
            return null;
        }

        return configs.stream()
                .map(IndependentServiceAssembler::toDTO)
                .collect(Collectors.toList());
    }
}
