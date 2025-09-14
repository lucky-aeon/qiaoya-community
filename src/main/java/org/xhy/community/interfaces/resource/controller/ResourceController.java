package org.xhy.community.interfaces.resource.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.dto.UploadCredentialsDTO;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.resource.request.GetUploadCredentialsRequest;
import org.xhy.community.interfaces.resource.request.ResourceQueryRequest;

import java.net.URI;

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
     * 获取资源访问链接
     * 通过资源ID获取带有临时访问权限的资源URL，并通过HTTP 302重定向到该URL
     * 适用于需要直接访问资源文件的场景，如图片预览、文档下载等
     * 
     * @param resourceId 资源ID，UUID格式的资源唯一标识符
     * @return HTTP 302重定向响应，Location头包含带有访问权限的资源URL
     */
    @GetMapping("/{resourceId}/access")
    public ResponseEntity<Void> accessResource(@PathVariable String resourceId) {
        String accessUrl = resourceAppService.getResourceAccessUrl(resourceId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(accessUrl))
                .build();
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