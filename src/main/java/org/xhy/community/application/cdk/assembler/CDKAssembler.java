package org.xhy.community.application.cdk.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.cdk.dto.CDKDTO;
import org.xhy.community.domain.cdk.entity.CDKEntity;

public class CDKAssembler {
    
    public static CDKDTO toDTO(CDKEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CDKDTO dto = new CDKDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static CDKDTO toDTOWithTargetName(CDKEntity entity, String targetName) {
        CDKDTO dto = toDTO(entity);
        if (dto != null) {
            dto.setTargetName(targetName);
        }
        return dto;
    }
}