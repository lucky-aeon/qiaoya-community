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

@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    
    private final ResourceAppService resourceAppService;
    
    public ResourceController(ResourceAppService resourceAppService) {
        this.resourceAppService = resourceAppService;
    }
    
    @PostMapping("/upload-credentials")
    public ApiResponse<UploadCredentialsDTO> getUploadCredentials(@Valid @RequestBody GetUploadCredentialsRequest request) {
        UploadCredentialsDTO credentials = resourceAppService.getUploadCredentials(
                request.getOriginalName(), 
                request.getContentType()
        );
        return ApiResponse.success(credentials);
    }
    
    @GetMapping("/{resourceId}/access")
    public ResponseEntity<Void> accessResource(@PathVariable String resourceId) {
        String accessUrl = resourceAppService.getResourceAccessUrl(resourceId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(accessUrl))
                .build();
    }
    
    @GetMapping("/my")
    public ApiResponse<PagedResourceDTO> getMyResources(@Valid ResourceQueryRequest request) {
        PagedResourceDTO resources = resourceAppService.getUserResources(request);
        return ApiResponse.success(resources);
    }
}