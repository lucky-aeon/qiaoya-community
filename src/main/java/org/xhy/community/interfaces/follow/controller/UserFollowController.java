package org.xhy.community.interfaces.follow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.follow.dto.FollowDTO;
import org.xhy.community.application.follow.service.FollowAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.follow.request.FollowQueryRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

/**
 * 用户关注管理控制器
 * 提供用户个人关注数据的管理功能
 * @module 关注管理
 */
@RestController
@RequestMapping("/api/user/follows")
public class UserFollowController {
    
    private final FollowAppService followAppService;
    
    public UserFollowController(FollowAppService followAppService) {
        this.followAppService = followAppService;
    }
    
    /**
     * 获取当前用户的关注列表
     * 分页查询当前登录用户关注的对象列表，支持按类型过滤
     * 需要JWT令牌认证
     * 
     * @param request 关注查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     *                - targetType: 关注目标类型过滤（可选），可选值：
     *                  * USER: 用户
     *                  * POST: 文章
     *                  * CHAPTER: 章节
     *                  * COURSE: 课程
     * @return 分页的关注列表（包含 targetType/targetId/targetName 等）
     */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "FOLLOW_LIST", name = "我的关注列表")})
    public ApiResponse<IPage<FollowDTO>> getMyFollowings(FollowQueryRequest request) {
        IPage<FollowDTO> followings = followAppService.getMyFollowings(request);
        return ApiResponse.success(followings);
    }
    
}
