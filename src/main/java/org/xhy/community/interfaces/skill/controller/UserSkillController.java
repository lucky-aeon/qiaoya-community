package org.xhy.community.interfaces.skill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.skill.dto.SkillDTO;
import org.xhy.community.application.skill.service.SkillAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.skill.request.CreateSkillRequest;
import org.xhy.community.interfaces.skill.request.SkillQueryRequest;
import org.xhy.community.interfaces.skill.request.UpdateSkillRequest;

@RestController
@RequestMapping("/api/user/skills")
public class UserSkillController {

    private final SkillAppService skillAppService;

    public UserSkillController(SkillAppService skillAppService) {
        this.skillAppService = skillAppService;
    }

    @GetMapping
    public ApiResponse<IPage<SkillDTO>> getMySkills(@Valid SkillQueryRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success(skillAppService.getUserSkills(currentUserId, request));
    }

    @PostMapping
    public ApiResponse<SkillDTO> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success("创建成功", skillAppService.createSkill(request, currentUserId));
    }

    @PutMapping("/{id}")
    public ApiResponse<SkillDTO> updateSkill(@PathVariable String id, @Valid @RequestBody UpdateSkillRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success("更新成功", skillAppService.updateSkill(id, request, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSkill(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        skillAppService.deleteSkill(id, currentUserId);
        return ApiResponse.success("删除成功");
    }
}
