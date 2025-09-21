package org.xhy.community.application.session.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.session.dto.ActiveSessionDTO;
import org.xhy.community.application.session.dto.UserSessionSummaryDTO;
import org.xhy.community.domain.session.valueobject.ActiveIpInfo;
import org.xhy.community.domain.user.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备会话转换器
 */
public class DeviceSessionAssembler {

    /**
     * 转换单个活跃IP信息到DTO
     */
    public static ActiveSessionDTO toActiveSessionDTO(ActiveIpInfo activeIpInfo) {
        if (activeIpInfo == null) {
            return null;
        }

        ActiveSessionDTO dto = new ActiveSessionDTO();
        dto.setIp(activeIpInfo.getIp());
        dto.setLastSeenTime(activeIpInfo.getLastSeenTime());
        dto.setCurrent(activeIpInfo.isCurrent());
        return dto;
    }

    /**
     * 转换活跃IP信息列表到DTO列表
     */
    public static List<ActiveSessionDTO> toActiveSessionDTOList(List<ActiveIpInfo> activeIpInfos) {
        if (activeIpInfos == null) {
            return null;
        }

        return activeIpInfos.stream()
                .map(DeviceSessionAssembler::toActiveSessionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换用户会话汇总DTO（管理员使用）
     */
    public static UserSessionSummaryDTO toUserSessionSummaryDTO(UserEntity user,
                                                                List<ActiveIpInfo> activeIps,
                                                                boolean isBanned) {
        if (user == null) {
            return null;
        }

        UserSessionSummaryDTO dto = new UserSessionSummaryDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMaxDevices(user.getMaxConcurrentDevices());
        dto.setActiveIpCount(activeIps != null ? activeIps.size() : 0);
        dto.setActiveIps(toActiveSessionDTOList(activeIps));
        dto.setBanned(isBanned);
        return dto;
    }
}