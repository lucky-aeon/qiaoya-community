package org.xhy.community.application.ops.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.ops.dto.BackupJobDTO;
import org.xhy.community.infrastructure.service.backup.BackupReportReader;

import java.util.ArrayList;
import java.util.List;

public class BackupAssembler {
    public static BackupJobDTO toDTO(BackupReportReader.BackupReport report) {
        if (report == null) return null;
        BackupJobDTO dto = new BackupJobDTO();
        dto.setDatabase(report.getDatabase());
        dto.setStartedAt(report.getStartedAt());
        dto.setFinishedAt(report.getFinishedAt());
        dto.setDurationSeconds(report.getDurationSeconds());
        dto.setMode(report.getMode());
        dto.setStatus(report.getStatus());
        if (report.getLocal() != null) {
            dto.setSizeBytes(report.getLocal().getSizeBytes());
            dto.setLocalFile(report.getLocal().getFile());
        }
        if (report.getRemote() != null) {
            dto.setRemoteUrls(report.getRemote().getUrls());
        }
        return dto;
    }

    public static List<BackupJobDTO> toDTOList(List<BackupReportReader.BackupReport> reports) {
        List<BackupJobDTO> list = new ArrayList<>();
        if (reports == null) return list;
        for (BackupReportReader.BackupReport r : reports) {
            list.add(toDTO(r));
        }
        return list;
    }
}

