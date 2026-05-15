package org.xhy.community.interfaces.course.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.course.dto.UserCourseOwnershipDTO;
import org.xhy.community.application.course.service.UserCourseOwnershipAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;

import java.util.List;

@RestController
@RequestMapping("/api/user/courses")
public class UserCourseOwnershipController {

    private final UserCourseOwnershipAppService userCourseOwnershipAppService;

    public UserCourseOwnershipController(UserCourseOwnershipAppService userCourseOwnershipAppService) {
        this.userCourseOwnershipAppService = userCourseOwnershipAppService;
    }

    /**
     * 当前用户课程权益列表
     */
    @GetMapping("/ownerships")
    public ApiResponse<List<UserCourseOwnershipDTO>> listCurrentUserCourseOwnerships() {
        String userId = UserContext.getCurrentUserId();
        return ApiResponse.success(userCourseOwnershipAppService.getCurrentUserCourseOwnerships(userId));
    }
}
