package org.xhy.community.application.ops.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.ops.assembler.BackupAssembler;
import org.xhy.community.application.ops.dto.BackupJobDTO;
import org.xhy.community.infrastructure.service.backup.BackupReportReader;
import org.xhy.community.interfaces.ops.request.BackupQueryRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminBackupAppService {
    private final BackupReportReader reportReader;

    public AdminBackupAppService(BackupReportReader reportReader) {
        this.reportReader = reportReader;
    }

    public BackupJobDTO getLatest() {
        return reportReader.latest().map(BackupAssembler::toDTO).orElse(null);
    }

    public IPage<BackupJobDTO> listReports(BackupQueryRequest request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        String status = request.getStatus();

        // 获取全部并按状态过滤与倒序排序
        List<BackupReportReader.BackupReport> all = reportReader.listAll();
        if (status != null && !status.isBlank()) {
            all = all.stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .collect(Collectors.toList());
        }

        long total = all.size();
        int from = Math.min(Math.max((pageNum - 1) * pageSize, 0), all.size());
        int to = Math.min(from + pageSize, all.size());
        List<BackupReportReader.BackupReport> pageList = all.subList(from, to);

        List<BackupJobDTO> dtos = BackupAssembler.toDTOList(pageList);
        Page<BackupJobDTO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(dtos);
        return page;
    }
}

