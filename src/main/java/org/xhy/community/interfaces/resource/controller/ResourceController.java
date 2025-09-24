package org.xhy.community.interfaces.resource.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.dto.UploadCredentialsDTO;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
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
    public ApiResponse<UploadCredentialsDTO> getUploadCredentials(@Valid @RequestBody GetUploadCredentialsRequest request) {
        UploadCredentialsDTO credentials = resourceAppService.getUploadCredentials(
                request.getOriginalName(), 
                request.getContentType()
        );
        return ApiResponse.success(credentials);
    }
    
    /**
     * 建立资源访问会话：签发短时效 HttpOnly Cookie (RAUTH)
     * 说明：前端以 Bearer 调用本接口，后端将 Bearer 中的token写入 HttpOnly Cookie，
     *       Path 受限在 /api/public/resource，用于元素请求(<img>/<a>)的鉴权。
     */
    @PostMapping("/access-session")
    public ResponseEntity<Void> createAccessSession(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authorization.substring(7);

        // 下发 HttpOnly Cookie，作用域仅限资源访问路径，时效短
        boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie cookie = ResponseCookie.from("RAUTH", token)
                .httpOnly(true)
                .secure(isSecure) // 本地开发HTTP下不设置Secure，生产启用HTTPS
                .sameSite("Lax")
                .path("/api/public/resource")
                .maxAge(900) // 15分钟
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }
    
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
    public ApiResponse<PagedResourceDTO> getUserResources(@Valid ResourceQueryRequest request) {
        PagedResourceDTO resources = resourceAppService.getUserResources(request);
        return ApiResponse.success(resources);
    }
}
