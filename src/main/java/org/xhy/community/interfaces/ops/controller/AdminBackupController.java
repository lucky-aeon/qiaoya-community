package org.xhy.community.interfaces.ops.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.ops.dto.BackupJobDTO;
import org.xhy.community.application.ops.service.AdminBackupAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.ops.request.BackupQueryRequest;

@RestController
@RequestMapping("/api/admin/backup")
public class AdminBackupController {

    private final AdminBackupAppService adminBackupAppService;

    public AdminBackupController(AdminBackupAppService adminBackupAppService) {
        this.adminBackupAppService = adminBackupAppService;
    }

    @GetMapping("/latest")
    public ApiResponse<BackupJobDTO> latest() {
        return ApiResponse.success(adminBackupAppService.getLatest());
    }

    @GetMapping("/list")
    public ApiResponse<IPage<BackupJobDTO>> list(@Valid BackupQueryRequest request) {
        return ApiResponse.success(adminBackupAppService.listReports(request));
    }
}

