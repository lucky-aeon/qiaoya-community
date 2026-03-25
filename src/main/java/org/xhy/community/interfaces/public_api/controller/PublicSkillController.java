package org.xhy.community.interfaces.public_api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.skill.dto.SkillDetailDTO;
import org.xhy.community.application.skill.dto.SkillListDTO;
import org.xhy.community.application.skill.dto.SkillStatsDTO;
import org.xhy.community.application.skill.service.SkillAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;

@RestController
@RequestMapping("/api/public/skills")
public class PublicSkillController {

    private final SkillAppService skillAppService;

    public PublicSkillController(SkillAppService skillAppService) {
        this.skillAppService = skillAppService;
    }

    @GetMapping
    public ApiResponse<IPage<SkillListDTO>> getSkills(@Valid SkillQueryRequest request) {
        return ApiResponse.success(skillAppService.queryPublicSkills(request));
    }

    @GetMapping("/stats")
    public ApiResponse<SkillStatsDTO> getSkillStats() {
        return ApiResponse.success(skillAppService.getSkillStats());
    }

    @GetMapping("/{id}")
    public ApiResponse<SkillDetailDTO> getSkillDetail(@PathVariable String id) {
        return ApiResponse.success(skillAppService.getPublicSkillById(id));
    }
}
