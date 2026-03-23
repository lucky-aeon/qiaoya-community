package org.xhy.community.interfaces.public_api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.skill.dto.SkillDTO;
import org.xhy.community.application.skill.service.PublicSkillAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;

@RestController
@RequestMapping("/api/public")
public class PublicSkillController {

    private final PublicSkillAppService publicSkillAppService;

    public PublicSkillController(PublicSkillAppService publicSkillAppService) {
        this.publicSkillAppService = publicSkillAppService;
    }

    @GetMapping("/skills")
    public ApiResponse<IPage<SkillDTO>> getSkills(@Valid SkillQueryRequest request) {
        return ApiResponse.success(publicSkillAppService.getPublicSkills(request));
    }
}
