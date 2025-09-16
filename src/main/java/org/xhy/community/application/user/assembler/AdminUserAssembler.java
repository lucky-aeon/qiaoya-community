package org.xhy.community.application.user.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.user.dto.AdminUserDTO;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.query.UserQuery;
import org.xhy.community.interfaces.user.request.AdminUserQueryRequest;

import java.util.List;

/**
 * 管理员用户转换器
 * 负责UserEntity与AdminUserDTO之间的转换
 */
public class AdminUserAssembler {
    
    /**
     * 从管理员用户查询请求创建用户查询对象
     */
    public static UserQuery fromQueryRequest(AdminUserQueryRequest request) {
        if (request == null) {
            return null;
        }
        
        UserQuery query = new UserQuery(request.getPageNum(), request.getPageSize());
        BeanUtils.copyProperties(request, query);
        return query;
    }
    
    /**
     * 将用户实体转换为管理员用户DTO
     */
    public static AdminUserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        AdminUserDTO dto = new AdminUserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    /**
     * 将用户实体列表转换为管理员用户DTO列表
     */
    public static List<AdminUserDTO> toDTOList(List<UserEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        return entities.stream()
                .map(AdminUserAssembler::toDTO)
                .toList();
    }
    
    /**
     * 将用户实体分页结果转换为管理员用户DTO分页结果
     */
    public static IPage<AdminUserDTO> toDTOPage(IPage<UserEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }
        
        Page<AdminUserDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AdminUserDTO> dtoList = toDTOList(entityPage.getRecords());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
}