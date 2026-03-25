package org.xhy.community.interfaces.skill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.skill.dto.SkillDetailDTO;
import org.xhy.community.application.skill.dto.SkillListDTO;
import org.xhy.community.application.skill.service.SkillAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
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

    @GetMapping("/my")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "SKILL_MY_LIST", name = "我的技能列表")})
    public ApiResponse<IPage<SkillListDTO>> getMySkills(@Valid SkillQueryRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success(skillAppService.queryMySkills(currentUserId, request));
    }

    @GetMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "SKILL_MY_DETAIL", name = "查看我的技能详情")})
    public ApiResponse<SkillDetailDTO> getSkill(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success(skillAppService.getUserSkillById(id, currentUserId));
    }

    @PostMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "SKILL_CREATE", name = "创建技能")})
    public ApiResponse<SkillDetailDTO> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success("创建成功", skillAppService.createSkill(request, currentUserId));
    }

    @PutMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "SKILL_UPDATE", name = "更新技能")})
    public ApiResponse<SkillDetailDTO> updateSkill(@PathVariable String id, @Valid @RequestBody UpdateSkillRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        return ApiResponse.success("更新成功", skillAppService.updateSkill(id, request, currentUserId));
    }

    @DeleteMapping("/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "SKILL_DELETE", name = "删除技能")})
    public ApiResponse<Void> deleteSkill(@PathVariable String id) {
        String currentUserId = UserContext.getCurrentUserId();
        skillAppService.deleteSkill(id, currentUserId);
        return ApiResponse.success("删除成功");
    }
}
