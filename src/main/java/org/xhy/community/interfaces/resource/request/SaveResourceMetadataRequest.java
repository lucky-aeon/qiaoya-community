package org.xhy.community.interfaces.resource.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SaveResourceMetadataRequest {
    
    @NotBlank(message = "文件键不能为空")
    private String fileKey;
    
    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String originalName;
    
    @NotBlank(message = "内容类型不能为空")
    @Size(max = 100, message = "内容类型长度不能超过100个字符")
    private String contentType;
    
    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须为正数")
    private Long size;
    
    public String getFileKey() {
        return fileKey;
    }
    
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
    
    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
}