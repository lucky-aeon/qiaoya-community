package org.xhy.community.interfaces.resource.controller;

import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.dto.UploadCredentialsDTO;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.interfaces.resource.request.GetUploadCredentialsRequest;
import org.xhy.community.interfaces.resource.request.ResourceQueryRequest;

/**
 * 资源管理控制器
 * 用户资源文件的上传、查询和访问功能
 * @module 资源管理
 */
@RestController
@RequestMapping("/api/user/resource")
public class ResourceController {
    
    private final ResourceAppService resourceAppService;
    
    public ResourceController(ResourceAppService resourceAppService) {
        this.resourceAppService = resourceAppService;
    }
    
    /**
     * 获取文件上传凭证
     * 获取阿里云OSS直传所需的临时凭证和签名信息
     * 支持的文件类型：图片(jpg/jpeg/png/gif/bmp/webp/svg)、视频(mp4/avi/mov/wmv/flv/webm/mkv)、
     * 音频(mp3/wav/flac/aac/ogg/wma)、文档(pdf/doc/docx/xls/xlsx/ppt/pptx/txt/rtf)等
     * 
     * @param request 上传凭证请求参数
     *                - originalName: 原始文件名，用于确定文件类型和生成存储路径
     *                - contentType: 文件MIME类型，如image/jpeg、application/pdf等
     * @return 上传凭证信息，包含STS临时凭证、OSS配置、上传策略和回调参数
     */
    @PostMapping("/upload-credentials")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "RESOURCE_UPLOAD_CREDENTIALS", name = "获取上传凭证")})
    public ApiResponse<UploadCredentialsDTO> getUploadCredentials(
            @Valid @RequestBody GetUploadCredentialsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String token = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        UploadCredentialsDTO credentials = resourceAppService.getUploadCredentials(
                request.getOriginalName(),
                request.getContentType(),
                token
        );
        return ApiResponse.success(credentials);
    }
    
    // 已删除 access-session 接口
    // RAUTH Cookie 现在在用户登录/注册时由 AuthController 自动设置
    // 有效期为 30 天，Domain 为 .xhyovo.cn，支持所有子域名（包括 oss.xhyovo.cn）

    /**
     * 分页查询当前用户的资源列表
     * 根据查询条件获取当前用户上传的资源文件列表，支持按资源类型筛选
     * 
     * @param request 资源查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     *                - resourceType: 资源类型过滤条件（可选），可选值：
     *                  * IMAGE: 图片类型
     *                  * VIDEO: 视频类型  
     *                  * DOCUMENT: 文档类型
     *                  * AUDIO: 音频类型
     *                  * OTHER: 其他类型
     * @return 分页资源列表，包含资源详情、总数、页码等分页信息
     */
    @GetMapping("/")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "RESOURCE_LIST", name = "查看我的资源")})
    public ApiResponse<PagedResourceDTO> getUserResources(@Valid ResourceQueryRequest request) {
        PagedResourceDTO resources = resourceAppService.getUserResources(request);
        return ApiResponse.success(resources);
    }
}
